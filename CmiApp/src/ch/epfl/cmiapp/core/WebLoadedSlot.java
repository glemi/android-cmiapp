package ch.epfl.cmiapp.core;

import org.jsoup.nodes.Element;
import ch.epfl.cmiapp.core.Configuration.Values;

public class WebLoadedSlot extends CmiSlot
{
	public WebLoadedSlot(Element td, Equipment equipment)
	{
		super(equipment);
		
		String statusStr = td.text(); // combined text of all children
		String dateTimeString = td.id().substring(0, 18);
		
		setTimeString(dateTimeString);
		
//		String hoverline = td.attr("onmouseover");
//		if (!hoverline.isEmpty())
//			Configuration.Values configValues = WebLoadedConfiguration.parseHoverLine(hoverline, equipment);
		
		//if (td.hasAttr("onmouseover"))
		//	slot.config = parseConfiguration(td.attr("onmouseover"));
		
		if (!td.select("input[type=checkbox]").isEmpty()) 
			super.status = CmiSlot.BookingStatus.REQUEST;
		else if	(statusStr.contains("Available")) 		
			super.status = CmiSlot.BookingStatus.AVAILABLE;
		else if (statusStr.contains("Impossible"))		
			super.status = CmiSlot.BookingStatus.INCOMPATIBLE;
		else if (statusStr.contains("Restricted"))		
			super.status = CmiSlot.BookingStatus.RESTRICTED;
		else if (statusStr.contains("maintenance"))		
			super.status = CmiSlot.BookingStatus.MAINTENANCE;
		else if (statusStr.contains("xxxxxxxxxxxxx"))	
			super.status = CmiSlot.BookingStatus.NOT_BOOKABLE;
		else
		{
			Element a = td.getElementsByTag("a").first();
			String href = a.attr("href");
			if (href.startsWith("javascript:GoAction('D'"))
			{
				super.status = BookingStatus.BOOKED_SELF;
				super.user = statusStr;
			}
			else if (href.startsWith("mailto:"))
			{
				super.status = BookingStatus.BOOKED;
				super.user = statusStr;
				super.email  = href.substring(7);
			}
		}
	}

}
