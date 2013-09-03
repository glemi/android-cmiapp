package ch.epfl.cmiapp;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

public class CmiReservation 
{
	
	public interface BookingCallback
	{
		public abstract void onBookingComplete();
	}
	
	Set<CmiSlot> delete = new HashSet<CmiSlot>();
	Set<CmiSlot> book   = new HashSet<CmiSlot>();
	
	String machId;
	String username;
	String password;
	
	BookingCallback callback;
	
	public CmiReservation(BookingCallback callback)
	{
		this.callback = callback;
	}
	
	public CmiReservation(String machId, String user, String password)
	{
		this.username = user;
		this.password = password;
	}
	
	public void setMachine(String machId)
	{
		this.machId = machId;
	}
	
	public void setCredentials(String user, String password)
	{
		this.username = user;
		this.password = password;
	}
	
	public boolean add(CmiSlot slot)
	{
		if (delete.contains(slot))
		{
			delete.remove(slot);
			return false;
		}
		else
		{
			book.add(slot);
			return true;
		}
	}
	
	public boolean remove(CmiSlot slot)
	{
		if (book.contains(slot))
		{
			book.remove(slot);
			return false; 
		}
		else
		{
			delete.add(slot);
			return true;
		}
	}
	
	public boolean isEmtpy()
	{
		return book.isEmpty() && delete.isEmpty();
	}
	
	public void clear()
	{
		book.clear();
		delete.clear();
	}
	
	@SuppressLint("DefaultLocale")
	public String report()
	{
		String rep = "";
		
		if (!book.isEmpty())
			rep = String.format("Booking %d slots", book.size());
		
		if (!book.isEmpty() && !delete.isEmpty())
			rep += String.format(", \n");
		
		if (!delete.isEmpty())
			rep += String.format("Un-booking %d slots", delete.size());
		
		return rep;
	}
	
	public void commit()
	{
		if (book.size() > 0)
		{
			JsoupBookingTask reservation = new JsoupBookingTask('R');
			Log.d("CmiReservation.commit()", "booking " + book.size() + " slots...");
			CmiSlot[] slots = book.toArray(new CmiSlot[book.size()]);
			reservation.execute(slots);
		}
		
		if (delete.size() > 0)
		{
			JsoupBookingTask deletion = new JsoupBookingTask('D');
			Log.d("CmiReservation.commit()", "deleting " + delete.size() + " slots...");
			CmiSlot[] slots = delete.toArray(new CmiSlot[book.size()]);
			deletion.execute(slots);
		}
	}
	
	private class BookingTask extends AsyncTask<CmiSlot, Integer, Boolean>
	{
		private char action = 'R';
		
		public BookingTask(char action)
		{
			this.action = action;
		}

		@Override
		protected Boolean doInBackground(CmiSlot... slots)
		{
			URL url;
			try
			{
				url = new URL("http://cmisrv1.epfl.ch/reservation/reserv.php");
			
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestMethod("POST");
				connection.setRequestProperty("charset", "iso-8859-1");
				connection.setDoOutput(true);
				
				for (CmiSlot slot : slots)
				{
					String resdat = URLEncoder.encode(slot.timeStamp, "iso-8859-1");
					
					OutputStream out = new BufferedOutputStream(connection.getOutputStream());
					PrintWriter writer = new PrintWriter(out);
					
					writer.append("login=cnyffeler&password=clemens&privilege=simple&ID_Machine=mach003&myaction=R&myresdat=2013-01-04+15%3A00%3A00");
					
//					12-28 20:33:48.237: D/CmiReservation.BookingTask.doInBackground(24220): user: cnyffeler password: clemens machId: mach003 date: 2013-01-04+11%3A00%3A00

//					writer.append("login=").append(username);
//					writer.append("&password=").append(password);
//					writer.append("&privilege=").append("simple");
//					writer.append("&ID_Machine=").append(machId);
//					writer.append("&myaction=").append(action);
//					writer.append("&myresdat=").append(slot.timeStamp);
					
					Log.d("CmiReservation.BookingTask.doInBackground", "user: " + username + " password: " + password + " machId: " + machId +" date: " + resdat);
					
					writer.flush();
					writer.close();
				}
				
				connection.disconnect();
				Log.d("CmiReservation.BookingTask.doInBackground", "no exception occurred (phew...)");
				return true;
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
			}
			catch (ProtocolException e)
			{
				e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
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
	    	callback.onBookingComplete();
	    }
	}
	
	private class JsoupBookingTask extends AsyncTask<CmiSlot, Integer, Boolean>
	{
		private String action = "R";
		
		public JsoupBookingTask(char action)
		{
			this.action = Character.toString(action);
		}

		@Override
		protected Boolean doInBackground(CmiSlot... slots)
		{
			Document document = null; // empty document
			Connection connection;

			String url = "http://cmisrv1.epfl.ch/reservation/reserv.php";

			try
			{
				for (CmiSlot slot : slots)
				{
					connection = Jsoup.connect(url);
					connection.data("login", username);
					connection.data("password", password);
					connection.data("privilege", "simple");
					connection.data("ID_Machine", machId);
					connection.data("myaction", action);
					connection.data("myresdat", slot.timeStamp);
					
					connection.post();
				}
			
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
	    	callback.onBookingComplete();
	    }
	}
	
}
