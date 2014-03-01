package ch.epfl.cmiapp.util;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.LocalDate;

import ch.epfl.cmiapp.core.CmiSchedule;
import ch.epfl.cmiapp.core.Configuration;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.ParcelableSchedule;
import ch.epfl.cmiapp.core.Schedule;
import ch.epfl.cmiapp.core.WebLoadedSchedule;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class CmiScheduleLoader extends AsyncTaskLoader<Schedule> 
{
	CmiAccount account;
	Equipment equipment;
	
	String startDate;
	String endDate;
	
	Configuration config = null;
		
	public CmiScheduleLoader(Context context)
	{
		super(context);
		CmiAccount account = CmiAccount.instance();
	}
	
	public void setMachId(String machId)
	{
		this.machId = machId;
	}
	
	public void setDate(LocalDate date)
	{
		setDateRange(date, date);
	}
	
	public void setDateRange(LocalDate dateStart, LocalDate dateEnd)
	{
		this.startDate = dateStart.toString("yyyy-MM-dd");
		this.endDate   = dateEnd.toString("yyyy-MM-dd");
	}
	
	public void setConfig(Configuration config)
	{
		this.config = config;
	}
	

	@Override
	public Schedule loadInBackground()
	{
		CmiServerConnection server = new CmiServerConnection(account);
		InputStream stream;
		Schedule schedule;
		
		String machId = equipment.getMachId();
		try
		{
			if (config != null)
				stream = server.getMainPage(machId, config);
			else
				stream = server.getMainPage(machId);
			
			 schedule = new WebLoadedSchedule(stream, equipment);
		}
		catch (IOException exception)
		{
			return null;
		}
		
		ParcelableSchedule parcelable = new ParcelableSchedule(schedule);
		
		return schedule;
	}
	
}
