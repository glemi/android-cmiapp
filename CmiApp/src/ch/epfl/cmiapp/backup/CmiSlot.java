package ch.epfl.cmiapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.*;
import org.joda.time.format.*;

public class CmiSlot 
	implements Comparable<CmiSlot>
{
	enum BookingStatus { AVAILABLE, BOOKED, BOOKED_SELF, RESTRICTED, MAINTENANCE, NOT_BOOKABLE }
	enum BookingAction { NONE, BOOK, UNBOOK }
	
	private DateTime start;
	private DateTime end;
	
	
	public String user   = "";
	public String machId = "";
	public Date   time   = null;
	public String timeStamp = "";
	
	public CmiEquipment equipment = new CmiEquipment();
	
	public BookingAction action = BookingAction.NONE;
	public BookingStatus status = BookingStatus.NOT_BOOKABLE;

	public boolean setTimeString(String timeString)
	{
		start = new DateTime();
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		start = formatter.parseDateTime(timeString);

		try
		{
			DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
			this.timeStamp = timeString;
			this.time = timeFormat.parse(timeString);
			return true;
		}
		catch (ParseException e)
		{
			time = null;
			timeStamp = "";
			return false;
		}
	}
	
	public String getTimeString()
	{
		return start.toString("HH:mm");
		
		
/*		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
		return timeFormat.format(calendar.getTime());
*/		
	}
	
	public String getDateString()
	{
		DateFormat timeFormat = new SimpleDateFormat("EEEE, MMM-dd", Locale.US);
		Calendar calendar = Calendar.getInstance();
		int today = calendar.get(Calendar.DAY_OF_YEAR);
		calendar.setTime(time);
		int date = calendar.get(Calendar.DAY_OF_YEAR);
		
		if (date == today)
			return "Today";
		else if (date == today + 1)
			return "Tomorrow";
		else
			return timeFormat.format(calendar.getTime());
	}
	
	public boolean isPast()
	{
		return time.getTime() < (new Date()).getTime() + 600000;
	}
	
	public int getDateOffset()
	{
		// TODO CmiSlot::getDateOffset 

		return 0;
	}
	
	public boolean equals(CmiSlot other)
	{
		return 	this.timeStamp == other.timeStamp && 
				this.status == other.status && 
				this.machId == other.machId;
	}

	public int compareTo(CmiSlot other )
	{
		

		
		
		return 0;
	}
	
}


	