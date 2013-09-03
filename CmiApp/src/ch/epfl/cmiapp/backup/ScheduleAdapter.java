package ch.epfl.cmiapp;

import java.text.*;
import java.util.*;

import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import android.content.Context;
import android.widget.*;
import android.util.Log;
import android.view.*;

public class ScheduleAdapter extends CmiPageAdapter 
{

	private int dateOffset = 0;
	private boolean noSlotsAvailable = false;
	
	private String machId;
	private List<CmiSlot> schedule = null;
	
	private boolean enableProgressIndicators = false;
	private boolean enableActionHighlights = false;
	
	public ScheduleAdapter()
	{
		super.setSectionsEnabled(false);
		schedule = new ArrayList<CmiSlot>();  
	}
	
	public void setDateOffset(int days)
	{
		dateOffset = days;
	}
	
	public void setMachId(String machId)
	{
		this.machId = machId;
	}

	public void setActionPending(int position, CmiSlot.BookingAction action)
	{
		schedule.get(position).action = action;
	}
	
	public void clearActionPending()
	{
		for (CmiSlot slot: schedule)
			slot.action = CmiSlot.BookingAction.NONE;
	}
	
	public void setDisplayProgressIndicators(boolean enable)
	{
		enableProgressIndicators = enable;
	}
	
	public void setActionHightlightEnabled(boolean enable)
	{
		enableActionHighlights = enable;
	}
	
	public boolean isWaitingForData()
	{
		return schedule.isEmpty() && !noSlotsAvailable;
	}
	

	public CmiSlot getItem(int position) 
	{
		return schedule.get(position);
	}

	public long getItemId(int position) 
	{
		return position;
	}
	
	@Override
	protected void onParseData(Document page) 
	{
		if (page == null)
			return;
		
		Element restable = page.select("table[id=restable]").first();
		Elements rows = restable.child(0).children();
		
		int columnIndex = dateOffset + 1;
		
		if (rows.size() > 0)
			schedule.clear();
		
		//Log.d("ScheduleAdapter.onParseData", "Date Offset = " + dateOffset + "; #rows = " + rows.size());
		//Log.d("ScheduleAdapter.onParseData", restable.html());
		
		for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++)
		{
			Element td = rows.get(rowIndex).child(columnIndex);
			
			String statusStr = td.text(); // combined text of all children
			String dateTimeString = td.id().substring(0, 18);
			
			Log.d("ScheduleAdapter.onParseData", "machId: " + machId + " timeStamp: " + dateTimeString);
			CmiSlot slot = CmiSlot.instantiate(machId, dateTimeString);
			if (slot == null)
				continue;
			
			td.select("a[href=^javascript:GoAction('D']").size();
			
			if 		(statusStr.contains("Available")) 		
				slot.status = CmiSlot.BookingStatus.AVAILABLE;
			else if (statusStr.contains("Restricted"))		
				slot.status = CmiSlot.BookingStatus.RESTRICTED;
			else if (statusStr.contains("maintenance"))
				slot.status = CmiSlot.BookingStatus.MAINTENANCE;
			else if (statusStr.contains("xxxxxxxxxxxxx"))
			{
				slot.status = CmiSlot.BookingStatus.NOT_BOOKABLE;
				continue; // do not add these slots to the view;
			}
			else if (td.children().size() > 0)
			{
				String href = td.children().first().attr("href");
				
				if (href.contains("javascript:GoAction('D'"))
				{
					slot.status = CmiSlot.BookingStatus.BOOKED_SELF;
					slot.user = statusStr;
				}
				else if (href.contains("mailto"))
				{
					slot.user   = statusStr;
					slot.email  = href.substring(7); // remove "mailto:"
					slot.status = CmiSlot.BookingStatus.BOOKED;
				}
				else // for completeness ... should never get executed
				{
					slot.user   = statusStr;
					slot.status = CmiSlot.BookingStatus.BOOKED;
				}
			}

			schedule.add(slot);
		}
		
		if (schedule.isEmpty() && rows.size() > 0)
		{
			// data has been received but no slots have been added
			noSlotsAvailable = true;
		}
	}

	@Override
	public String getEmptyText() 
	{
		return new String("Schedule is empty? WTF?!");
	}


	@SuppressWarnings("unused")
	@Override
	public View getItemView(int position, View convertView, ViewGroup parent) 
	{
		//Log.d("ScheduleAdapter.getView", "start ");
		Context context = parent.getContext();
		Object service = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LayoutInflater inflater = (LayoutInflater) service;	
		
		View row = convertView;
		if (row == null)
			row = inflater.inflate(R.layout.schedule_listitem, parent, false);

		CmiSlot slot = getItem(position);
		
		TextView 	timeView    = (TextView)    row.findViewById(R.id.slotTime);
		TextView 	statusView  = (TextView)    row.findViewById(R.id.slotStatus);
		ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.progressBar);

		DateFormat timeFormat = new SimpleDateFormat("HH:mm");
		
		timeView.setText(slot.getTimeString());
		
		int availableColor    = parent.getResources().getColor(R.color.bookingAvailable);
		int restrictedColor   = parent.getResources().getColor(R.color.bookingRestricted);
		int maintenanceColor  = parent.getResources().getColor(R.color.bookingMaintenance);
		int bookedSelfColor   = parent.getResources().getColor(R.color.bookingSelf);
		int bookedColor       = parent.getResources().getColor(R.color.bookingBooked);
		
		int bookActionColor   = parent.getResources().getColor(R.color.bookAction);
		int unbookActionColor = parent.getResources().getColor(R.color.unbookAction);
		
		switch (slot.status)
		{
		case AVAILABLE:
			statusView.setText("available");
			statusView.setTextColor(row.getResources().getColor(R.color.bookingAvailable));
			//Log.d("ScheduleAdapter.getView", "Item " + position + ": Available");
			break;
		case RESTRICTED: 
			statusView.setText("restricted");
			statusView.setTextColor(row.getResources().getColor(R.color.bookingRestricted));
			//row.setBackgroundColor(0xF03030); // SOME RED
			//statusView.setBackgroundColor(0xF03030);
			//Log.d("ScheduleAdapter.getView", "Item " + position + ": Restricted");
			break;
		case MAINTENANCE:
			statusView.setText("maintenance");
			statusView.setTextColor(row.getResources().getColor(R.color.bookingMaintenance));
			//Log.d("ScheduleAdapter.getView", "Item " + position + ": Maintenance");
			break;
		case BOOKED_SELF:
			statusView.setText(slot.user);
			statusView.setTextColor(row.getResources().getColor(R.color.bookingSelf));
			break;
		case BOOKED:
			statusView.setText(slot.user);
			statusView.setTextColor(row.getResources().getColor(R.color.bookingBooked));
			//Log.d("ScheduleAdapter.getView", "Item " + position + ": Booked");
			break;
		case NOT_BOOKABLE: // NO SLOT HAVING status = NOT_BOOKABLE MAY EXIST!
							// THIS CODE SHOULD NEVER BE EXECUTED!
			statusView.setText("");
			//Log.d("ScheduleAdapter.getView", "Item " + position + ": Not available");
			//statusView.setBackgroundColor(0xC0C0C0);
			//row.setBackgroundColor(0xC0C0C0);
		}
		
		if(slot.status == CmiSlot.BookingStatus.NOT_BOOKABLE)
		{	// NO SLOT HAVING status = NOT_BOOKABLE MAY EXIST!
			// THIS CODE SHOULD NEVER BE EXECUTED!
			ListView listView = (ListView) parent.findViewById(R.id.scheduleList);
	    	View emptyView = parent.findViewById(R.id.emptyNoSlots);
	    	listView.setEmptyView(emptyView);
		} 
		
		
		if (slot.isPast())
		{
			 // TODO add formatting past slot
		}
		
		if (slot.isNow())
		{
			// TODO add formatting NOW-slot
		}
		
		if (enableActionHighlights)
		{
			switch (slot.action)
			{
				case NONE:	 row.setBackgroundColor(0);						break;
				case BOOK:	 row.setBackgroundColor(bookActionColor);		break;
				case UNBOOK: row.setBackgroundColor(unbookActionColor);		break;
			}
		}
		else row.setBackgroundColor(0);	

		if (enableProgressIndicators && slot.action != CmiSlot.BookingAction.NONE)
		{
			progressBar.setVisibility(View.VISIBLE);
			//statusView.setVisibility(View.INVISIBLE);
		}
		else
		{
			progressBar.setVisibility(View.INVISIBLE);
			//statusView.setVisibility(View.VISIBLE);
		}
		
		row.setTag(slot);
		
		return row;
	}

	@Override
	public boolean isEnabled(int position)
	{
		switch (schedule.get(position).status)
		{
		case BOOKED_SELF:
		case AVAILABLE:
			return true;
		default:
			return false;
		} 
	}

	@Override
	public int getItemCount() 
	{
		if(schedule == null)
			return 0;
		else
			return schedule.size();
	}


	@Override
	public String getSection(int position) 
	{
		return "";
	}
}
