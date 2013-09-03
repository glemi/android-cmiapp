package ch.epfl.cmiapp;

import java.io.*;
import java.util.*;

import org.joda.time.LocalDateTime;
import org.jsoup.*;
import ch.epfl.cmiapp.CmiSlot.BookingAction;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import ch.epfl.cmiapp.CmiEquipment.Configuration;

public class CmiReservation 
	implements Comparable<CmiReservation>
{
	public static final long CMI_DATA_TYPE_ID = 1l;
	
	public interface BookingCallback
	{
		public abstract void onBookingComplete();
	}
	
	protected SortedSet<CmiSlot> slots = new TreeSet<CmiSlot>();
	
	private String username;
	private String password;
	
	private Configuration config;
	
	BookingCallback callback;
	
	public CmiReservation()
	{
		
	}
	
	public CmiReservation(BookingCallback callback)
	{
		this.callback = callback;
	}
	
	public void setCredentials(String user, String password)
	{
		this.username = user;
		this.password = password;
	}
	
	public void setConfiguration(CmiEquipment.Configuration config)
	{
		this.config = config;
	}
	
	public void setBookingCallback(BookingCallback callback)
	{
		this.callback = callback;
	}
	
	public void insertSlot(CmiSlot slot)
	{
		slots.add(slot);
	}
	
	public void insertSlot(CmiSlot slot, CmiSlot.BookingAction action)
	{
		slot.action = action;
		slots.add(slot);
	}
	
	public CmiSlot.BookingAction toogleBooking(CmiSlot slot)
	{
		// TODO: prevent booking problem 
		// if the page has been reloaded the action field of the
		// supplied slot may have been reset to NONE and may be out
		// of sync. This is a potential problem.
		
		if (slot.action == BookingAction.NONE)
			switch (slot.status)
			{
				case AVAILABLE:		slot.action = BookingAction.BOOK;    break;
				case REQUEST: 		slot.action = BookingAction.REQUEST; break;
				case BOOKED_SELF:	slot.action = BookingAction.UNBOOK;  break;
			}
		else 
		{
			slot.action = BookingAction.NONE;
		}
		
		if (!slots.contains(slot))
			slots.add(slot);
		else if (slot.action == BookingAction.NONE)
			slots.remove(slot);
		
		return slot.action;
	}
	
	public void unbookAll()
	{
		for (CmiSlot slot : slots)
			slot.action = BookingAction.UNBOOK;
		
		commit();
	}
	
	public int getSlotCount()
	{
		return slots.size();
	}
	
	public boolean isEmtpy()
	{
		return slots.isEmpty();
	}
	
	public LocalDateTime getStartTime()
	{
		return slots.first().getStartTime();
	}
	
	public LocalDateTime getEndTime()
	{
		return slots.last().getEndTime();
	}
	
	public String getMachId()
	{
		if(slots.isEmpty())
			return "";
		else
			return slots.first().getMachId();
	}
	
	public long getID()
	{
		/*  How to construct id's for reservations:
		 *  Relevant Data:
		 *  > Code for Reservation [1 Byte]
		 *  > Start time (e.g. # of minutes from 2010) [3 Bytes]
		 *     Ten years of minutes:
		 *     60 x 24 x 365 x 10 = 5'256'000 < 2^23 = 8'388'608
		 *     3 Bytes or 24 bits are plenty for this
		 *  > Machine ID [2 Bytes] (if ever we'll have more than 256)
		 *  
		 *  Joda-Time counts miliseconds from 1970-01-01 
		 *  
		 */
		long millis_from_1970_to_2010 	= 1262304000000l; 
		long millis_of_slot 			= getStartTime().toDateTime().getMillis();
		long millis_per_minute			= 60000l;
		
		long minutes = (millis_of_slot - millis_from_1970_to_2010) / millis_per_minute;
		
		long machId = Long.parseLong(this.getMachId().substring(4));
		
		return CmiReservation.CMI_DATA_TYPE_ID | machId << 8 | minutes << 16;
	}
	
	public boolean isContiguous()
	{
		if (slots.isEmpty())
			return false;
		
		Iterator<CmiSlot> iterator = slots.iterator();
		CmiSlot slot = iterator.next();
		
		while (iterator.hasNext())
		{
			CmiSlot next = iterator.next();
			if (!slot.isAdjacent(next))
				return false;
		}
		
		return true;
	}
	
	public boolean isNow()
	{
		if (isContiguous()) for (CmiSlot slot : slots)
			if (slot.isNow()) return true;
		return false;
	}
	
	public String[] getSlotTimeStamps()
	{
		int count = slots.size();
		String[]  timeStamps = new String[count]; 
		CmiSlot[] slotArray = new CmiSlot[count]; 
		
		slots.toArray(slotArray);

		for(int i = 0; i < count; i++)
			timeStamps[i] = slotArray[i].getTimeStamp();
		
		return timeStamps;
	}
	
	public void clear()
	{
		slots.clear();
	}
	
	@SuppressLint("DefaultLocale")
	public String report()
	{
		String rep = "";
		int nBook = 0;
		int nUnbook = 0;
		
		for (CmiSlot slot : slots)
			switch (slot.action)
			{
				case BOOK: nBook++;		break;
				case UNBOOK: nUnbook++; break;
			}

		if (nBook > 0)
			rep = String.format("Booking %d slots", nBook);
		
		if (nBook > 0 && nUnbook > 0)
			rep += String.format(", \n");
		
		if (nUnbook > 0)
			rep += String.format("Un-booking %d slots", nUnbook);
		
		return rep;
	}
	
	public void commit()
	{
		if (username == null || password == null)
			throw new RuntimeException("User Credentials were not specified");
		
		if (slots.size() > 0)
		{
			JsoupBookingTask bookingTask = new JsoupBookingTask();
			bookingTask.execute();
		}
	}

	private class JsoupBookingTask extends AsyncTask<Void, Integer, Boolean>
	{

		@Override
		protected Boolean doInBackground(Void... par)
		{
			Connection connection;

			String url = "http://cmisrv1.epfl.ch/reservation/reserv.php";

			try
			{
				for (CmiSlot slot : slots)
				{
					connection = Jsoup.connect(url);
					char action = 'R';
					
					switch (slot.action)
					{
					case UNBOOK: action = 'D'; 
					case   BOOK: 
									
						connection.data("login", username);
						connection.data("password", password);
						connection.data("privilege", "simple");
						connection.data("ID_Machine", slot.getMachId());
						connection.data("myaction", String.valueOf(action));
						connection.data("myresdat", slot.getTimeStamp());
						
						if (config != null)
							for (Configuration.Setting setting : config.settings)
								connection.data(setting.id, setting.currentValue);		
					}
					connection.post();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}			
			
			if (config != null) if (config.settings.size() > 0) try
			{
				/* if a configurable machine is reserved 
				 * login=cnyffeler
				 * &password=clemens
				 * &ID_Machine=mach126
				 * &list0=331									<<< parameter 1
				 * &list1=338									<<< parameter 2
				 * &confdate[]=2013-04-24+10:30:00				<<< slot 1
				 * &confdate[]=2013-04-23+09:30:00				<<< slot 2
				 * &confdate[]=2013-04-24+11:30:00				<<< slot 3
				 * &request=<b>Configuration request</b><b>U... <<< email text (containing crlf)
				 * &daterequest=2013-07-25 09:00:00				<<< all time stamps separated by crlf
				 * &confokbut=Continue							<<< signals definitive booking
				 * &mode=calendar
				*/
				
				connection = Jsoup.connect(url);
				connection.data("login", username);
				connection.data("password", password);
				connection.data("ID_Machine", getMachId());
				connection.data("confokbut", "Continue"); // trigger booking
				connection.data("mode", "calendar");
				
				String emailText = generateEmailText();
				connection.data("request", emailText);
				
				for (Configuration.Setting setting : config.settings)
					connection.data(setting.id, setting.currentValue);
				
				int requestSlotCount = 0;
				for (CmiSlot slot : slots) if(slot.action == BookingAction.REQUEST)
				{	
					requestSlotCount++;
					connection.data("confdate[]", slot.getTimeStamp());		// date-time
				}
				
				Log.d("CmiReservation.JsoupBookingTask", connection.toString());
				if (requestSlotCount > 0)
					connection.post();

				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}	
			
			return false;
		}
		
		protected void onProgressUpdate(Integer... progress) 
		{
			
	    }

	    protected void onPostExecute(Boolean result) 
	    {
	    	if (callback != null)
	    		callback.onBookingComplete();
	    }

	}
	
	public String generateEmailText()
	{
		
		/* What the result of this function should look like:
		 * 
		 * <b>Configuration request</b>
		 * <b>User : </b>cnyffeler
		 * <b>Email : </b>clemens.nyffeler@epfl.ch
		 * <b>Equipment : </b>Z09 Outil-Sableuse
		 * <b>Requested configuration :</b>
		 * sable : Sable blanc
		 * <b>Slots :</b>
		 * 2013-07-25 09:00:00
		 * 2013-07-25 10:00:00
		 * 2013-07-23 11:00:00
		 * 2013-07-23 12:00:00
		 * 2013-07-23 13:00:00
		 * 2013-07-26 14:00:00	 
		*/
		
		String eqptString;
		CmiEquipment eqpt = CmiEquipment.getEquipmentByMachId(this.getMachId());
		if (eqpt != null)
			eqptString = "Z" + eqpt.zone + " " + eqpt.fullString;
		else
			throw new RuntimeException();
		
		String text = "\r\n<b>Configuration request</b>\r\n";
		text += "<b>User : </b>" + this.username + "\r\n";
		text += "<b>Email : </b> " + "\r\n";  
		text += "Equipment : </b>" +  eqptString + "\r\n";
		text += "<b>Requested configuration : </b>\r\n";
		
		for (Configuration.Setting setting : config.settings)
		{
			if (setting.getCurrent().value != "0" )
			text += setting.name + " : " + setting.getCurrent().name + "\r\n";
		}
		
		text += "<b>Slots : </b>\r\n";
		
		for (CmiSlot slot : slots) if (slot.action == BookingAction.REQUEST)
		{
			text += slot.getTimeStamp() + "\r\n";
		}
		
		text += "\r\n\r\n\r\n   **** THIS IS A TEST **** \r\n";
		text += "EMAIL GENERATED BY ANDROID CELL PHONE APPLICATION\r\n";
		text += "in case of problems, please contact clemens.nyffeler@epfl.ch\r\n";
		
		return text;
	}

	public int compareTo(CmiReservation other)
	{
		/*if (!this.getMachId().equals(other.getMachId()))
			return this.getMachId().compareTo(other.getMachId());
		else
			return this.getStartTime().compareTo(other.getStartTime());
		 */
		
		if (!this.getStartTime().equals(other.getStartTime()))
			return this.getStartTime().compareTo(other.getStartTime());
		else
			return this.getMachId().compareTo(other.getMachId());
	}
	
}
