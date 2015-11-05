package ch.epfl.cmiapp.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Schedule
{
	public class SlotList extends ArrayList<CmiSlot>
	{
		public SlotList(Collection<? extends CmiSlot> collection)
		{
			super(collection);
		}

		public SlotList() {}
	}

	private Equipment equipment; 
	
	private List<LocalDate> dates = new ArrayList<LocalDate>();
	private NavigableMap<LocalDate, SlotList> slotsByDate = new TreeMap<LocalDate, SlotList>();
	private NavigableMap<LocalDateTime, CmiSlot> slotsByTime = new TreeMap<LocalDateTime, CmiSlot>();

	private LocalTime firstSlotTime = new LocalTime(23, 59); // initial value is latest possible time
	private LocalTime lastSlotTime = new LocalTime(00, 00); // initial value is earliest possible time
	
	private int slotDurationMinutes = 30; // Default value: 30min.
	private int slotsPerDayCount = 0;
	
	protected class Builder
	{
		public Builder(){};
		
		public void addSlot(CmiSlot slot)
		{
			LocalDateTime dateTime = slot.getStartTime();
			LocalDate date = dateTime.toLocalDate();
			LocalTime time = dateTime.toLocalTime(); 
			
			slotsByTime.put(dateTime, slot);
			SlotList slotList = slotsByDate.get(date);
			
			if (slotList == null)
			{
				slotList = new SlotList();
				slotsByDate.put(date, slotList);
				dates.add(date);
			}
			slotList.add(slot);
			
			if (time.isBefore(firstSlotTime)) firstSlotTime = time;
			if (time.isAfter(lastSlotTime)) lastSlotTime = time;
			if (slotsPerDayCount < slotList.size()) 
				slotsPerDayCount = slotList.size();
		}
	}
	
	protected CmiSlot[] allSlots()
	{
		int n = slotsByTime.size();
		CmiSlot[] array = new CmiSlot[n];
		slotsByTime.values().toArray(array);
		return array;
	}
	
	protected Equipment getEquipment()
	{
		return equipment;
	}
	
	public Schedule(Equipment equipment)
	{
		Schedule.this.equipment = equipment;
		slotDurationMinutes = equipment.getSlotLength();
	}
	
	// Shallow Copy
	public Schedule(Schedule other)
	{
		this.equipment = other.equipment;
		this.slotsByTime = other.slotsByTime;
		this.slotsByDate = other.slotsByDate;
		this.dates = other.dates;
		this.firstSlotTime = other.firstSlotTime;
		this.lastSlotTime = other.lastSlotTime;
		this.slotDurationMinutes = other.slotDurationMinutes;
	}
	
	public SlotList getSlotsBetween(LocalDateTime start, LocalDateTime end)
	{
		Collection<CmiSlot> slots = slotsByTime.subMap(start, end).values();
		return new SlotList(slots);
	}
	
	public SlotList getSlots(LocalDate date)
	{	
		return slotsByDate.get(date);
	}
	
	public SlotList getSlots(int dateIndex)
	{
		LocalDate date = dates.get(dateIndex);
		return slotsByDate.get(date);
	}
	
	public LocalDate getDate(int dateIndex)
	{
		return dates.get(dateIndex);
	}
	
	public boolean isEmpty()
	{
		return slotsByTime.isEmpty();
	}
	
	public int getSlotPosition(String timeStamp)
	{
		LocalTime time = null;
		
		try {
			time = LocalTime.parse(timeStamp);
			return getSlotPosition(time);
		} catch (java.lang.IllegalArgumentException excp) {}
		
		try {
			time = LocalDateTime.parse(timeStamp).toLocalTime();
			return getSlotPosition(time);
		} catch (java.lang.IllegalArgumentException excp) {}
		
		try {
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			time = formatter.parseLocalDateTime(timeStamp).toLocalTime();
			return getSlotPosition(time);
		} catch (java.lang.IllegalArgumentException excp) {}
		
		return -1;
	}
	
	public int getSlotPosition(LocalTime time)
	{
		Period diff = new Period(this.firstSlotTime, time);
		int diffMinutes =  diff.getHours()*60 + diff.getMinutes(); 
		int slotPosition = diffMinutes / slotDurationMinutes; // rounds towards 0
		return slotPosition;
	}
	
	public int getNowSlotPosition()
	{
		// returns -1 if no slots exists at this time
		LocalTime now = LocalTime.now();
		
		if (now.isBefore(firstSlotTime))
			return -1;
		else if (now.isAfter(lastSlotTime.plusMinutes(slotDurationMinutes-1)))
			return -1;
		else
			return getSlotPosition(now);
	}
	
	public int getSlotsPerDayCount()
	{
		return slotsPerDayCount;
	}
	
	public int getDayCount()
	{
		return dates.size();
	}
	
	/* Merge is asymmetric. In case of collision slots of schedule1 will be 
	 * retained and slots of schedule2 will be discarded. See CmiSlot.merge().
	 */
	public static Schedule merge(Schedule schedule1, Schedule schedule2) 
	{
		String machid1 = schedule1.equipment.getMachId();
		String machid2 = schedule2.equipment.getMachId();
		
		if (!machid1.equals(machid2))
			throw new ScheduleMergeException("Schedule objects belong to different machines.");
		
		Schedule schedule = new Schedule(schedule1.equipment);
		Builder builder = schedule.new Builder(); // weird syntax but it's like that!
		
		NavigableMap<LocalDateTime, CmiSlot> slots1 = new TreeMap<LocalDateTime, CmiSlot>();
		NavigableMap<LocalDateTime, CmiSlot> slots2 = new TreeMap<LocalDateTime, CmiSlot>();
		
		slots1.putAll(schedule1.slotsByTime);
		slots2.putAll(schedule2.slotsByTime);
		
		// iterate over slots in schedule1, if corresponding slot 
		// in schedule2 exists, merge them. 
		for (Map.Entry<LocalDateTime, CmiSlot> entry : slots1.entrySet())
		{
			LocalDateTime dateTime = entry.getKey();
			CmiSlot slot1 = entry.getValue();
			CmiSlot slot = slot1;
			
			if (slots2.containsKey(dateTime))
			{
				CmiSlot slot2 = slots2.get(dateTime);
				slot = CmiSlot.merge(slot1, slot2);
				slots2.remove(dateTime);
			}
			builder.addSlot(slot);
		}
		
		// Iterate over remaining slots in schedule2
		for (CmiSlot slot : slots2.values())
			builder.addSlot(slot);
		
		return schedule;
	}
	
	@Override
	public String toString()
	{
		return printSchedule();
	}
	
	private String printSchedule()
	{
		String output = "";
		int nDays = getDayCount();
		int chunkSize = 7;
		
		for (int iChunk = 0; iChunk <= nDays/chunkSize; iChunk++)
		{
			int start = iChunk*chunkSize;
			int end = start + chunkSize -1;
			end = end >= nDays ? nDays-1 : end;
			output += printScheduleChunk(start, end);
		}
		return output;
	}
	
	private String printScheduleChunk(int start, int end)
	{
		int nSlots = getSlotsPerDayCount();
		String row = "";
		
		for (int iDate = start; iDate <= end; iDate++)
		{
			LocalDate date = getDate(iDate);
			row += String.format("%15s ", date.toString("EEE"));
		}
		String chunk = row + "\n"; 
		
		for (int iSlot = 0; iSlot < nSlots; iSlot++)
		{
			row = "";
			for (int iDate = start; iDate <= end; iDate++)
			{
				SlotList slots = getSlots(iDate);
				
				CmiSlot slot = slots.get(iSlot);
				row += String.format("%15s ", slot.toString());
			}
			chunk += row + "\n";
		}
		return chunk + "\n";
	}
	
	public static class ScheduleMergeException extends RuntimeException
	{
		public ScheduleMergeException(String message, Throwable cause)
		{
			super("Error merging Schedules: " + message, cause);
		}
		
		public ScheduleMergeException(String message)
		{
			super("Error merging Schedules: " + message);
		}
	}
}
