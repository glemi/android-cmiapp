package ch.epfl.cmiapp.adapters;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import ch.epfl.cmiapp.CmiSchedule;
import ch.epfl.cmiapp.CmiSlot;
import ch.epfl.cmiapp.R;

// http://cyrilmottier.com/2011/08/08/listview-tips-tricks-3-create-fancy-listviews/

public class SlotListAdapter extends BaseAdapter
{
	private CmiSchedule schedule = null;
	private List<CmiSlot> slotList = null;
	
	private LocalDateTime start;
	private LocalDateTime end;
	
	private List<LocalDateTime> highlightList = new ArrayList<LocalDateTime>();
	
	private boolean enableProgressIndicators = false;
	private boolean enableActionHighlights = false;
	private boolean enableAddDummySlots = true;
	
	private static final LocalTime morning   = LocalTime.parse("08:00");
	private static final LocalTime noon      = LocalTime.parse("11:59");
	private static final LocalTime afternoon = LocalTime.parse("12:59");
	private static final LocalTime evening   = LocalTime.parse("17:00");
	
	private static final int STD_HEIGHT_DP = 36;
	private static final int STD_SLOT_DURATION_MIN = 30;
	
	private static final int STANDARD_SLOT = 0;
	private static final int MARGINAL_SLOT = 1;
	private static final int STANDARD_SLOT_NOW = 2;
	private static final int MARGINAL_SLOT_NOW = 3;
	
	private static Drawable standard_bg = null;  
	private static Drawable dimmed_bg 	= null;   
	private static Drawable stdNow_bg   = null;   
	private static Drawable dimNow_bg	= null; 
	
	private static void loadBackgrounds(Resources res)
	{
		standard_bg = res.getDrawable(R.drawable.highlight);
		dimmed_bg   = res.getDrawable(R.drawable.highlight_dimmed);
		stdNow_bg   = res.getDrawable(R.drawable.nowslot_default);
		dimNow_bg	= res.getDrawable(R.drawable.nowslot_dimmed);
	}
	
	private Drawable getBackground(Resources res, int type, boolean reload)
	{
		Drawable background = null;
		if (standard_bg == null)
		{
			loadBackgrounds(res);
			reload = false;
		}
		
		if (reload)
			switch (type)
			{
				case MARGINAL_SLOT_NOW: background = res.getDrawable(R.drawable.nowslot_dimmed); break;
				case STANDARD_SLOT_NOW: background = res.getDrawable(R.drawable.nowslot_default); break;
				case MARGINAL_SLOT:     background = res.getDrawable(R.drawable.highlight_dimmed); break;
				case STANDARD_SLOT:     background = res.getDrawable(R.drawable.highlight); break;
			}
		else
			switch (type)
			{
				case MARGINAL_SLOT_NOW: background = dimNow_bg; break;
				case STANDARD_SLOT_NOW: background = stdNow_bg; break;
				case MARGINAL_SLOT:     background = dimmed_bg; break;
				case STANDARD_SLOT:     background = standard_bg; break;
			}
		return background;
	}
	
	
	public void setContent(CmiSchedule schedule, LocalDateTime start, LocalDateTime end)
	{
		this.schedule = schedule;
		this.start = start;
		this.end = end;
		
		refresh();
	}
		
	public int getCount()
	{
		refresh(); // bisschen ineffizient aber soweit kein problem...
		
		if (enableAddDummySlots)
			return schedule.getSlotsPerDay();
			
		if (this.slotList == null)
			return 0;
		else
			return slotList.size();
	}
	
	private void refresh()
	{
		slotList = schedule.getSlotsBetween(start, end);
		
		if (enableAddDummySlots)
		{
			int diff = schedule.getSlotsPerDay() - slotList.size(); 
			
			for (int i = 0; i < diff; i++)
				slotList.add(schedule.createDummySlot());
		}
	}
	
	public Object getItem(int position)
	{
		return slotList.get(position);
	}
	
	public CmiSlot getSlot(int position)
	{
		return slotList.get(position);
	}

	public long getItemId(int position)
	{
		// doesn't work with dummy slots...
		return 0; //slotList.get(position).getStartTime().toDateTime().getMillis();
	}

	@Override
	public boolean isEnabled(int position)
	{
		CmiSlot slot = slotList.get(position);
		if (slot.isPast() || slot.isNow())
			return false;
			
		switch (slot.status)
		{
		case BOOKED_SELF:
		case AVAILABLE:
		case REQUEST:
			return true;
		default:
			return false;
		} 
	}

	public int getCenterItemPosition()
	{
		for (int position = 0; position < slotList.size(); position++)
		{
			CmiSlot slot = slotList.get(position);
			if (slot.isNow())
				return position;
		}
		
		for (int position = 0; position < slotList.size(); position++)
		{
			CmiSlot slot = slotList.get(position);
			LocalTime start = slot.getStartTime().toLocalTime();
			LocalTime   end = slot.getEndTime().toLocalTime();
			
			if (start.isAfter(noon) && start.isBefore(afternoon))
				return position;		
		}
		
		if (slotList.size() > 2)
			return (int) Math.round(slotList.size() / 2d);
		else
			return 0;
	}
	

	public void highlightSlots(String[] timeStamps)
	{
		highlightList.clear();
		for (int k = 0; k < timeStamps.length; k++)
		{
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime ldt = formatter.parseLocalDateTime(timeStamps[k]);
			
			if (ldt.isAfter(start) && ldt.isBefore(end))
				highlightList.add(ldt);
		}
		notifyDataSetChanged();
	}
	
	public void setActionPending(int position, CmiSlot.BookingAction action)
	{
		slotList.get(position).action = action;
	}
	
	public void clearActionPending()
	{
		for (CmiSlot slot: slotList)
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
	
	public void setAddDummySlots(boolean enable)
	{
		enableAddDummySlots = enable;
	}
	
	@Override
	public int getViewTypeCount()
	{
		return 4;
	}
	
	public int getSlotType(CmiSlot slot)
	{
		if (slot.isNow())
			if (slot.isMarginal()) 	return MARGINAL_SLOT_NOW;
			else					return STANDARD_SLOT_NOW;
		else
			if (slot.isMarginal())  return MARGINAL_SLOT;
			else					return STANDARD_SLOT;
	}
	
	@Override
	public int getItemViewType(int position)
	{
		CmiSlot slot = getSlot(position);
		return getSlotType(slot);		
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Context context 		= parent.getContext();
		Resources res 			= context.getResources();
		Object service  		= context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LayoutInflater inflater = (LayoutInflater) service;	

		CmiSlot slot = getSlot(position);
		
		View row = convertView;
		if (row == null)
		{
			row = inflater.inflate(R.layout.schedule_listitem, parent, false);
			Drawable background = getBackground(res, getItemViewType(position), true);
			row.setBackgroundDrawable(background);
		}
		
		setRowText(row, slot, res);
		setRowBackground(row, slot, res);
		setRowHeight(row, slot, res);
		row.setTag(slot);
		return row;
	}

	
	void setRowText(View row, CmiSlot slot, Resources res)
	{
		TextView timeView    = (TextView) row.findViewById(R.id.slotTime);
		TextView statusView  = (TextView) row.findViewById(R.id.slotStatus);
		
		timeView.setText(slot.getTimeString());
		
		switch (slot.status)
		{
		case AVAILABLE:
			statusView.setText("available");
			statusView.setTextColor(res.getColor(R.color.bookingAvailable));
			//Log.d("ScheduleAdapter.getstatusView", "Item " + position + ": Available");
			break;
		case REQUEST:
			statusView.setText("request");
			statusView.setTextColor(res.getColor(R.color.bookingAvailable));
			break;
		case RESTRICTED: 
			statusView.setText("restricted");
			statusView.setTextColor(res.getColor(R.color.bookingRestricted));
			//row.setBackgroundColor(0xF03030); // SOME RED
			//statusView.setBackgroundColor(0xF03030);
			//Log.d("ScheduleAdapter.getView", "Item " + position + ": Restricted");
			break;
		case MAINTENANCE:
			statusView.setText("maintenance");
			statusView.setTextColor(res.getColor(R.color.bookingMaintenance));
			//Log.d("ScheduleAdapter.getView", "Item " + position + ": Maintenance");
			break;
		case BOOKED_SELF:
			statusView.setText(slot.user);
			statusView.setTextColor(res.getColor(R.color.bookingSelf));
			break;
		case BOOKED:
			statusView.setText(slot.user);
			statusView.setTextColor(res.getColor(R.color.bookingBooked));
			break;
		case INCOMPATIBLE:
			statusView.setText("incompatible");
			statusView.setTextColor(res.getColor(R.color.bookingBooked));
			break;
		case NOT_BOOKABLE: // NO SLOT HAVING status = NOT_BOOKABLE MAY EXIST!
							// THIS CODE SHOULD NEVER BE EXECUTED!
			statusView.setText("");
			break;
		case DUMMY:
			statusView.setText("");
			timeView.setText("");
			row.setEnabled(false);
		}
		
		if (slot.isPast())
		{
			statusView.setTextColor(res.getColor(R.color.bookingRestricted));
		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	void setRowBackground(View row, CmiSlot slot, Resources res)
	{
		Drawable background = null; 
		
		if (enableActionHighlights)
		{
			switch (slot.action)
			{
			case NONE:	 
				background = getBackground(res, getSlotType(slot), true);
				row.setBackgroundDrawable(background);
				break;
			case BOOK:	
			case REQUEST:
				row.setBackgroundColor(res.getColor(R.color.bookAction));	
				Log.d("slotListAdapter.setRowBackground", "setting background to 'book'");
				break;
			case UNBOOK: 
				row.setBackgroundColor(res.getColor(R.color.unbookAction));		
				break;
			}
			loadBackgrounds(res);
		}
		else 
		{	
			if (highlightList.contains(slot.getStartTime()))
			{
				Log.d("SlotListAdapter.setRowBackground", "slot is being highlighted!!");
				// TransitionDrawable highlight = (TransitionDrawable) res.getDrawable(R.drawable.highlight);
				// row.setBackgroundColor(res.getColor(android.R.color.holo_purple));
				// row.setBackgroundResource(R.drawable.highlight);
				TransitionDrawable highlight = (TransitionDrawable) row.getBackground();
				//row.setBackgroundDrawable(highlight);
				highlight.resetTransition();
				highlight.startTransition(0);
				highlight.reverseTransition(600);
				highlightList.remove(slot.getStartTime());
			}
			/*else
			{
				background = getBackground(res, getSlotType(slot), false);
				row.setBackgroundDrawable(background);
			}*/
		}
		
		
		
		ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.progressBar);
		if (slot.isPast())
		{
			row.setEnabled(false);
			// TODO add formatting past slot
		}

		if (enableProgressIndicators && slot.action != CmiSlot.BookingAction.NONE)
			progressBar.setVisibility(View.VISIBLE);
		else
			progressBar.setVisibility(View.INVISIBLE);
	}
	
	void setRowHeight(View row, CmiSlot slot, Resources res)
	{
		float duration = slot.getDurationMinutes();
		float height = (float) (STD_HEIGHT_DP + 0.5 * (duration - STD_SLOT_DURATION_MIN) * STD_HEIGHT_DP / STD_SLOT_DURATION_MIN);
		
		DisplayMetrics metrics = res.getDisplayMetrics();
		float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, metrics);
		
		ViewGroup.LayoutParams params = row.getLayoutParams();
		params.height = Math.round(pixels);
		row.setLayoutParams(params);
	}
	
	
	/*
		int availableColor  = res.getColor(R.color.bookingAvailable);
		int restrictedColor   = res.getColor(R.color.bookingRestricted);
		int maintenanceColor  = res.getColor(R.color.bookingMaintenance);
		int bookedSelfColor   = res.getColor(R.color.bookingSelf);
		int bookedColor       = res.getColor(R.color.bookingBooked);
		
		int bookActionColor   = res.getColor(R.color.bookAction);
		int unbookActionColor = res.getColor(R.color.unbookAction);
		
		
		
		if(slot.status == CmiSlot.BookingStatus.NOT_BOOKABLE)
		{	// NO SLOT HAVING status = NOT_BOOKABLE MAY EXIST!
			// THIS CODE SHOULD NEVER BE EXECUTED!
			ListView listView = (ListView) parent.findViewById(R.id.scheduleList);
	    	View emptyView = parent.findViewById(R.id.emptyNoSlots);
	    	listView.setEmptyView(emptyView);
		} 
	*/
	
}
