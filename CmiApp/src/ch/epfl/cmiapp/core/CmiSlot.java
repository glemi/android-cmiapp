package ch.epfl.cmiapp.core;

import org.joda.time.*;
import org.joda.time.format.*;


import android.util.Log;

public class CmiSlot 
	implements Comparable<CmiSlot>
{
	public enum BookingStatus { AVAILABLE, REQUEST, BOOKED, BOOKED_SELF, RESTRICTED, MAINTENANCE, NOT_BOOKABLE, DUMMY, INCOMPATIBLE }
	public enum BookingAction { NONE, BOOK, UNBOOK, REQUEST }
	
	private LocalDateTime start;
	private LocalDateTime end;
	private String timeStamp = "";
	
	private String machId = "";
	private Equipment equipment = null;
	
	public String user   = "";
	public String email  = "";
	
	public BookingAction action = BookingAction.NONE;
	public BookingStatus status = BookingStatus.NOT_BOOKABLE;
	
	public Configuration config;
	
	private static final LocalTime morning   = LocalTime.parse("08:00");
	private static final LocalTime noon      = LocalTime.parse("11:59");
	private static final LocalTime afternoon = LocalTime.parse("12:59");
	private static final LocalTime evening   = LocalTime.parse("17:00");
	
	public static CmiSlot create(Equipment equipment, String timeStamp)
	{
		CmiSlot slot = null;
		try
		{
			slot = new CmiSlot(equipment, timeStamp);
		}
		catch (Exception e)
		{
			Log.d("CmiSlot.create", "Cannot instantiate CmiSlot - time stamp format not recognized");
		}
		return slot;
	}
	
	public static CmiSlot createDummy(int duration_minutes)
	{
		CmiSlot slot = new CmiSlot();
		slot.status = BookingStatus.DUMMY;
		slot.machId = "mach999"; // make ensure dummies go last in sorting 
		slot.start = new LocalDateTime(9999, 1, 1, 0, 0); // distant future (year 9999)
		slot.end   = slot.start.plusMinutes(duration_minutes);
		return slot;
	}
	
	private CmiSlot() {} // prevent use of default constructor
	
	public CmiSlot(Equipment equipment, String timeStamp)
	{
		this.equipment = equipment;
		this.machId = equipment.getMachId();
		
		if(!setTimeString(timeStamp))
			throw new RuntimeException("Cannot instantiate CmiSlot - time stamp format not recognized");
		

	}
	
	private boolean setTimeString(String timeString)
	{
		int duration = equipment.slotLength;

		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		start = new LocalDateTime();
		start = formatter.parseLocalDateTime(timeString.trim());
		end   = start.plusMinutes(duration);
		
		this.timeStamp = timeString;
		return true;
	}
	
	public String getMachId() 			{ return equipment.machId; }
	public Equipment getEquipment()		{ return equipment; }
	public String getTimeString() 		{ return start.toString("HH:mm"); }	
	public LocalDateTime getStartTime() { return start; }
	public LocalDateTime getEndTime()	{ return end; }
	public String getTimeStamp() 		{ return timeStamp; }
	public int getDurationMinutes()		{ return Minutes.minutesBetween(start, end).getMinutes(); }
	
	public String getDateString()
	{		
		switch (getDateOffset())
		{
			case 0:  return "Today";
			case 1:  return "Tomorrow";
			default: return start.toString("EEEE, MMM-dd");
		}
	}
	
	public boolean isPast()
	{
		LocalDateTime now = new LocalDateTime();
		return end.isBefore(now);
	}
	
	public boolean isNow()
	{
		LocalDateTime now = new LocalDateTime();
		return start.isBefore(now) && end.isAfter(now);
	}
	
	public boolean isMarginal()
	{
		LocalTime start = this.start.toLocalTime(); 
		LocalTime end   = this.end.toLocalTime();
		
		boolean early = end.isBefore(morning);
		boolean late  = start.isAfter(evening);
		boolean lunch = start.isAfter(noon) && start.isBefore(afternoon);
		
		return ((early || late || lunch) && getDurationMinutes() <= 60);
	}
	
	public boolean isAdjacent(CmiSlot other)
	{
		if (!this.machId.equals(other.machId))
			return false;
		else
			return this.end.equals(other.start);
	}
	
	public int getDateOffset()
	{
		LocalDate slotDate = start.toLocalDate();
		LocalDate today = new LocalDate();
		
		return Days.daysBetween(today, slotDate).getDays();
	}
	
	public boolean equals(CmiSlot other)
	{
		return 	this.timeStamp == other.timeStamp && 
				this.status == other.status && 
				this.machId == other.machId;
	}

	public int compareTo(CmiSlot other)
	{	
		if (!this.machId.equals(other.machId))
			return this.machId.compareTo(other.machId);
		else
			return this.start.compareTo(other.start);
	}
	
}


	