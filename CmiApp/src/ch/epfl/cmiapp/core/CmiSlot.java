package ch.epfl.cmiapp.core;

import org.joda.time.*;
import org.joda.time.format.*;


import android.util.Log;

public class CmiSlot 
	implements Comparable<CmiSlot>
{
	public enum BookingStatus { AVAILABLE, REQUEST, BOOKED, BOOKED_SELF, RESTRICTED, MAINTENANCE, NOT_BOOKABLE, DUMMY, INCOMPATIBLE }
	public enum BookingAction { NONE, BOOK, UNBOOK, REQUEST }
	
	protected LocalDateTime start;
	protected LocalDateTime end;
	protected String timeStamp = "";
	
	protected String machId = "";
	protected Equipment equipment = null;
	
	public String user   = "";
	public String email  = "";
	
	protected BookingAction action = BookingAction.NONE;
	protected BookingStatus status = BookingStatus.NOT_BOOKABLE;
	
	public Configuration config;
	public Configuration.Values configValues;
	
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
	
	public CmiSlot(CmiSlot other)
	{
		this.equipment = other.equipment;
		this.start = other.start;
		this.end   = other.end;
		this.machId = other.machId;
		this.user = other.user;
		this.email = other.email;
		this.timeStamp = other.timeStamp;
		this.action = other.action;
		this.status = other.status;
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
	
	protected CmiSlot(Equipment equipment)
	{
		this.equipment = equipment;
		this.machId = equipment.getMachId();
	}
	
	public CmiSlot(Equipment equipment, String timeStamp)
	{
		this.equipment = equipment;
		this.machId = equipment.getMachId();
		
		if(!setTimeString(timeStamp))
			throw new RuntimeException("Cannot instantiate CmiSlot - time stamp format not recognized");
	}
	
	protected boolean setTimeString(String timeString)
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
	
	// Slot merge is not symmetric. Values of slot1 are dominant over values 
	// of slot2, except if the value of a field is undefined in slot1 and
	// is defined in slot2. 
	public static CmiSlot merge(CmiSlot slot1, CmiSlot slot2)
	{
		if (slot1.equipment != slot2.equipment)
			return null;
		
		if (slot1.timeStamp.equals(slot2.timeStamp))
			return null;
		
		CmiSlot slot = new CmiSlot(slot1.equipment, slot1.timeStamp);
		
		slot.action = slot1.action;
		slot.status = slot1.status;
		
		if (slot1.configValues == null && slot2.configValues != null)
			slot.configValues = slot2.configValues;
		else 
			slot.configValues = slot1.configValues;
		
		if (slot1.user.isEmpty() && !slot2.user.isEmpty())
			slot.user = slot2.user;
		else
			slot.user = slot1.user;
		
		if (slot1.email.isEmpty() && !slot2.email.isEmpty())
			slot.email = slot2.email;
		else
			slot.email = slot1.email;
		
		return slot;
	}
//	
//	public String user   = "";
//	public String email  = "";
//	
//	public BookingAction action = BookingAction.NONE;
//	public BookingStatus status = BookingStatus.NOT_BOOKABLE;
//	
//	public Configuration config;
//	public Configuration.Values configValues;
//	
}


	