package ch.epfl.cmiapp.core;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;
import ch.epfl.cmiapp.core.CmiSchedule.Day;
import ch.epfl.cmiapp.core.CmiSlot.BookingStatus;
import ch.epfl.cmiapp.util.CmiLoader;
import ch.epfl.cmiapp.util.CmiServerConnection;
import ch.epfl.cmiapp.util.CmiServerConnection.PageType;

public class WebLoadedSchedule extends Schedule
{
	private static final int LAST_COLUMN_INDEX = 12;
	
	private Builder builder;
	private Equipment equipment;
	
	public WebLoadedSchedule(InputStream stream, Equipment equipment) throws IOException
	{
		super(equipment);
		builder = new Schedule.Builder();
		this.equipment = equipment;
		load(stream);
	}
	
	private boolean load(InputStream stream) throws IOException
	{
		Document document = Jsoup.parse(stream, null, "");
		
		if (document.title().equalsIgnoreCase("all reservations"))
			parseResPage(document);
		else
			parseMainPage(document);

		return true;
	}
	
	private void parseMainPage(Document document)
	{
		Element restable = document.getElementById("restable");
		Elements rows = restable.child(0).children();
		
		for (int columnIndex = 1; columnIndex < LAST_COLUMN_INDEX; columnIndex++)
		{
			for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++)
			{
				Element td = rows.get(rowIndex).child(columnIndex);
				CmiSlot slot = new WebLoadedSlot(td, equipment);

				if (slot == null) continue;
				if (slot.status == CmiSlot.BookingStatus.NOT_BOOKABLE) continue;
					
				builder.addSlot(slot);
			}
		}
	}
	
	
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
			
			CmiSlot slot = new CmiSlot(equipment, dateTimeString);

			slot.user = userString;
			slot.status = BookingStatus.BOOKED;
			
			builder.addSlot(slot);
		}
	}
}
