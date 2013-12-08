package ch.epfl.cmiapp.core;


import java.util.*;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.cmiapp.CmiLoader;
import ch.epfl.cmiapp.CmiLoader.PageType;

import android.util.Log;

public class CmiSchedule
{
	public static class Day extends ArrayList<CmiSlot> 
	{
		private static final long	serialVersionUID	= -8047183662943980787L;
	}
	
	private LocalDate[] dates;
	private NavigableMap<LocalDate, Day> days = new TreeMap<LocalDate, Day>();
	private NavigableMap<LocalDateTime, CmiSlot> slotMap = new TreeMap<LocalDateTime, CmiSlot>();
	
	private LocalTime firstSlotTime = new LocalTime(23,59); // 23:59h  
	private LocalTime lastSlotTime = new LocalTime(00, 00);
	private int slotsPerDay;
	private int slotDurationMinutes;
	
	private String machId;
	private String userName;
	//private CmiEquipment eqpt;
	
	boolean noSlotsAvailable = false;
	
	private static final int LAST_COLUMN_INDEX = 12;
	
	
	public CmiSchedule(String machId, String userName)
	{
		this.machId = machId;
		this.userName = userName;
		//eqpt = CmiEquipment.getEquipmentByMachId(machId);
	}
	
	private CmiSchedule(){} 
	
	public List<CmiSlot> getSlotsAt(int position)
	{	
		LocalDate date = getDateAt(position);
		if (date == null) return null;
		return days.get(date);
	}
	
	public List<CmiSlot> getSlotsBetween(LocalDateTime start, LocalDateTime end)
	{
		Collection<CmiSlot> slots = slotMap.subMap(start, end).values();
		List<CmiSlot> slotList = new ArrayList<CmiSlot>();
		slotList.addAll(slots);
		return slotList;
	}
	
	public List<CmiSlot> getSlotsBetween(String startString, String endString)
	{
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime start = formatter.parseLocalDateTime(startString.trim());
		LocalDateTime end   = formatter.parseLocalDateTime(endString.trim());
		return getSlotsBetween(start, end);
	}
	
	public List<CmiSlot> getSlotsOn(DateMidnight date)
	{	
		return days.get(date);
	} 
	
	public List<CmiSlot> getSlotsOn(String dateString)
	{	
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		DateMidnight date = formatter.parseDateTime(dateString.trim()).toDateMidnight();
		return days.get(date);
	} 
	
	public CmiSlot getSlot(String datetimeStr)
	{
		LocalDateTime dateTime = LocalDateTime.parse(datetimeStr);
		return slotMap.get(dateTime);
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
	
	
	public String getMachId()
	{
		return machId;
	}
	
	public LocalDate getDateAt(int index)
	{
		if (days.size() <= index)
			return null;
		else
			return dates[index];
	}
	
	public int getPositionOf(LocalDate date)
	{
		for (int i = 0; i < dates.length; i++)
			if (date.equals(dates[i])) return i;
		return -1;
	}
	
	public int getPositionOf(String timeStamp)
	{
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		LocalDate date = formatter.parseDateTime(timeStamp.trim()).toLocalDate();
		return getPositionOf(date);
	}
	
	public int getDayCount()
	{
		return days.size();
	}
	
	public int getSlotsPerDay()
	{
		return slotsPerDay;
	}
	
	public int getSlotDuration()
	{
		return slotDurationMinutes;		
	}
	
	public CmiSlot createDummySlot()
	{
		return CmiSlot.createDummy(slotDurationMinutes);
	}
	
	public boolean parseDocument(Document document, CmiLoader.PageType pageType)
	{
		if (document == null) return false;
		switch (pageType)
		{
		case MAIN_PAGE_RES:
			parseMainPage(document); break;
		case ALL_RESERVATIONS_PAGE:
			parseResPage(document); break;
			
		}
		
		return true;
	}
	
	private void parseMainPage(Document document)
	{
		Element restable = document.select("table[id=restable]").first();
		Elements rows = restable.child(0).children();
		
		if (rows.size() > 0) slotMap.clear();
		
		//Log.d("ScheduleAdapter.onParseData", "Date Offset = " + dateOffset + "; #rows = " + rows.size());
		//Log.d("ScheduleAdapter.onParseData", restable.html());
		
		for (int columnIndex = 1; columnIndex < LAST_COLUMN_INDEX; columnIndex++)
		{
			for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++)
			{
				Element td = rows.get(rowIndex).child(columnIndex);
				CmiSlot slot = parseSlot(td);
				
				if (slot == null) continue;
				if (slot.status == CmiSlot.BookingStatus.NOT_BOOKABLE) continue;
					
				LocalDateTime datetime = slot.getStartTime();
				slotMap.put(datetime, slot);
				
				LocalTime time = datetime.toLocalTime();
				if (time.isBefore(firstSlotTime))
					firstSlotTime = time;
				
				if (time.isAfter(lastSlotTime))
					lastSlotTime = time;
				
				LocalDate date = datetime.toLocalDate();
				if (!days.keySet().contains(date))
				{
					Day day = new Day();
					day.add(slot);
					days.put(date, day);
				}
				else
				{
					Day day = days.get(date);
					if (rowIndex == 1)
						day.clear(); 
					
					day.add(slot);
					if (slotsPerDay < day.size())
						slotsPerDay = day.size();
				}
			}
		}
		
		if (slotMap.isEmpty() && rows.size() > 0)
		{	// data has been received but no slots were added
			noSlotsAvailable = true;
			slotDurationMinutes = 30; // default;
		}
		else
		{
			slotDurationMinutes = slotMap.firstEntry().getValue().getDurationMinutes();
		}
		
		dates = new LocalDate[days.size()];
		days.keySet().toArray(dates);
	}
	
	private CmiSlot parseSlot(Element td)
	{
		String statusStr = td.text(); // combined text of all children
		String dateTimeString = td.id().substring(0, 18);
		
		//Log.d("CmiSchedule.parseSlot", "machId: " + machId + " timeStamp: " + dateTimeString);
		CmiSlot slot = CmiSlot.instantiate(machId, dateTimeString);
		
		if (slot == null) return null;
		
		//if (td.hasAttr("onmouseover"))
		//	slot.config = parseConfiguration(td.attr("onmouseover"));
		
		if (!td.select("input[type=checkbox]").isEmpty()) slot.status = CmiSlot.BookingStatus.REQUEST;
		else if	(statusStr.contains("Available")) 		slot.status = CmiSlot.BookingStatus.AVAILABLE;
		else if (statusStr.contains("Impossible"))		slot.status = CmiSlot.BookingStatus.INCOMPATIBLE;
		else if (statusStr.contains("Restricted"))		slot.status = CmiSlot.BookingStatus.RESTRICTED;
		else if (statusStr.contains("maintenance"))		slot.status = CmiSlot.BookingStatus.MAINTENANCE;
		else if (statusStr.contains("xxxxxxxxxxxxx"))	slot.status = CmiSlot.BookingStatus.NOT_BOOKABLE;
		else if (statusStr.contains(userName)) 
		{
			slot.status = CmiSlot.BookingStatus.BOOKED_SELF;
			slot.user = statusStr;
		}
		else 
		{
			slot.status = CmiSlot.BookingStatus.BOOKED;
			slot.user = statusStr;
			
			Element link = td.select("a[href]").first();
			
			if (link != null)
			{
				String href = link.attr("href");
				slot.email  = href.substring(7); // remove "mailto:"
			}
		}
		
		return slot;
	}
	
	/*private CmiEquipment.Configuration parseConfiguration(String string)
	{
		// parse the onmouseover attribute
		// onmouseover="this.style.cursor='pointer';return escape('E-beam #1: Al<br>E-beam #2: Cr<br>E-beam #3: Ti<br>Th. Evap #2: Au<br>');"
		
		CmiEquipment.Configuration config = new CmiEquipment.Configuration();
		
		Pattern pattern = Pattern.compile("escape\\('(.*)'\\)");
		Matcher matcher = pattern.matcher(string);
		
		String configStr = matcher.group(1);
		
		String[] settingStrings = configStr.split("<br>");
		
		for (String settingString : settingStrings)
		{
			String[] subStrs = settingString.split(":\\s");
			if (subStrs.length == 2)
			{
				String settName = subStrs[0];
				String optName  = subStrs[1];
				
				CmiEquipment.Configuration.Setting setting = eqpt.config.findSetting(settName); s
				if (setting == null) continue;
				CmiEquipment.Configuration.Option option = setting.findOption(optName);
				if (option == null) continue;
				
				// !!! Important !!! Clone setting before changing currentValue !!! 
				//setting = new CmiEquipment.Configuration.Setting(setting); // clone
				//setting.currentValue = option.value;
			}
		}
		
		return null;
	}*/
	

	private void parseResPage(Document document)
	{
		Element restable = document.select("table[style]").first();
		Elements rows = restable.select("tr");
		
		for (int i = 1; i < rows.size(); i++)
		{
			Element row = rows.get(i);
			Element tdUser = row.child(0);
			Element tdTime = row.child(2);
			
			String dateTimeString = tdTime.ownText().substring(0, 18);
			String userString = tdUser.ownText().replace('\u00a0', ' ').trim();
			
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime key = formatter.parseLocalDateTime(dateTimeString);
			
			CmiSlot slot = slotMap.get(key);
			if (slot != null && slot.status == BookingStatus.RESTRICTED)
			{
				Log.d("CmiSchedule.parseResPage", "updating slot on " + dateTimeString + ", " +  slot.user + " <= " + userString);
				slot.user = userString;
				slot.status = BookingStatus.BOOKED;
			}
		}
		
	}
}
