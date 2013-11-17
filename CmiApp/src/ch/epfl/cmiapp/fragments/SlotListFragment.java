package ch.epfl.cmiapp.fragments;

import java.util.List;

import org.joda.time.*;

import ch.epfl.cmiapp.CmiSlot;
import ch.epfl.cmiapp.ListScrollSyncer;
import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.ScheduleManager;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;
import ch.epfl.cmiapp.ScheduleManager.State;
import ch.epfl.cmiapp.ScheduleManager.onStateChangedListener;
import ch.epfl.cmiapp.adapters.SlotListAdapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;

public class SlotListFragment extends Fragment 
	implements onStateChangedListener
{
	private SlotListAdapter adapter = null;
	private ScheduleManager scheduleManager;
	private ListScrollSyncer listScrollSyncer;
	private Bundle arguments;
	
	private LocalDateTime start;
	private LocalDateTime end;
	
	private ListView listView    = null;
	private TextView titleView   = null;
	private View noSlotsView     = null;
	private View loadingView     = null;
	private View emptyView		 = null;
	
	private int position;
	private boolean showTitle = false;
	private int currentPosition = -1;
	
	private boolean centerScrollPosition = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
			this.arguments = savedInstanceState;
		else
			this.arguments = this.getArguments();
		
		Fragment fragment = this.getActivity().getSupportFragmentManager().findFragmentByTag("SCHEDULE");
		if (fragment != null && fragment instanceof ScheduleMasterFragment)
		{
			ScheduleMasterFragment master = (ScheduleMasterFragment) fragment;
			this.scheduleManager = master.getScheduleManager();
			this.scheduleManager.registerOnStateChangedListener(this);
			this.listScrollSyncer = master.getListScrollSyncer();
		}
		else
		{
			throw new RuntimeException("SlotListFragment must be " +
					"the child of a ScheduleMasterFragment with Tag set to 'schedule'");
		}
		
		readArguments();
		adapter = scheduleManager.getAdapter(start, end);
		
		super.onCreate(savedInstanceState);
	}
	
  	 @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        View view   = inflater.inflate(R.layout.schedule_fragment, container, false);
        
        listView    = (ListView) view.findViewById(R.id.scheduleList);
    	loadingView = 			 view.findViewById(R.id.emptyLoading);        	
    	noSlotsView = 			 view.findViewById(R.id.emptyNoSlots);
    	titleView   = (TextView) view.findViewById(R.id.title);
    	
    	TextView debugText;
    	debugText   = (TextView) view.findViewById(R.id.debugText);
    	debugText.setText(start.toString("MM-dd"));
    	
    	listView.setAdapter(adapter);
    	//listView.setEmptyView(loadingView);
    	listView.setOnItemLongClickListener(scheduleManager);
    	listView.setOnItemClickListener(scheduleManager);
    	listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
    	Log.d("SlotListFragment.onCreateView", "initial startScrollUpdate");
    	//centerViewVertically();
    	//listView.post(new ScrollPositionSetter());
    	
    	if (this.listScrollSyncer != null)
    		listScrollSyncer.addList(listView);
    	
    	//Log.d("SlotListFragment.onCreateView", "SHOW_TITLE : " + showTitle);
    	titleView.setText(start.toString("EEEE MMM-dd"));
    	titleView.setVisibility( showTitle ? View.VISIBLE : View.GONE );
    	
        //updateEmptyView();
    	//loadingView.setVisibility(View.INVISIBLE);
    	//noSlotsView.setVisibility(View.INVISIBLE);
    	
    	//Log.d("SlotListFragment.onCreateView", "Date = " + start.toString("MM-dd"));
        return view;
	}
  	 
  	 
  	public boolean centerViewVertically()
  	{
  		if (adapter.getCount() == 0 || !this.getUserVisibleHint())
  			return false;
  			
  		int position = adapter.getCenterItemPosition();
  		int height = listView.getHeight();
  		
  		height = this.getActivity().getWindowManager().getDefaultDisplay().getHeight();
  		
  		int offset = (int) Math.round(height*0.35);
  		
  		listScrollSyncer.setScrollPosition(position, offset);
  		
  		Log.d("SlotListFragment.centerViewVertically", 
  			String.format("adapter: %d items, center item: %d, listView Hieght: %d, computed Offset: %d", adapter.getCount(), position, height, offset));
  		return true;
  	}
  	
  	
  	 @Override
  	public void onDestroyView()
  	{
     	if (this.listScrollSyncer != null)
     		listScrollSyncer.removeList(listView);
  		super.onDestroyView();
  	}
  	 
	public void onStateChanged(State newState)
	{
		//scheduleManager now does all that's commented out here
		Log.d("SlotListFragment.onStateChanged", "[" + start.toString("E, dd") + "] new state = " + newState.toString());
		switch (newState)
		{
		case WAITING_FOR_DATA:
			getActivity().setProgressBarIndeterminate(true);
			break;
		case IDLE:
			getActivity().setProgressBarIndeterminate(false);
			
			//startScrollUpdate();
			//listView.post(new ScrollPositionSetter());
			//adapter.setActionHightlightEnabled(false);
			//adapter.setDisplayProgressIndicators(false);
			//adapter.notifyDataSetChanged();
			break;
		case BOOKING_MODE:
			//adapter.setActionHightlightEnabled(true);
			//adapter.notifyDataSetChanged();
			break;
		case BOOKING_COMMIT:
			//adapter.setActionHightlightEnabled(false);
			//adapter.setDisplayProgressIndicators(true);
			//adapter.notifyDataSetChanged();
			break;
		case WAITING_FOR_RELOAD:
			
		}
	}
	 
	private void readArguments()
	{
		LocalDate date      = (LocalDate) arguments.getSerializable("DATE");
		LocalTime startTime = (LocalTime) arguments.getSerializable("START_TIME");
		LocalTime   endTime = (LocalTime) arguments.getSerializable("END_TIME");
		
		showTitle = arguments.getBoolean("SHOW_TITLE");
		Log.d("SlotListFragment.readArguments", "SHOW_TITLE : " + showTitle);
		
		start = date.toLocalDateTime(startTime);
		end   = date.toLocalDateTime(endTime);
	}
	
	public void querySlots()
	{
		List<CmiSlot> slotList;
		slotList = scheduleManager.getSchedule().getSlotsBetween(start, end);
	}
	 
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putAll(arguments);
		super.onSaveInstanceState(outState);
	}

	public void setShowTitle(boolean enable)
	{
		showTitle = enable;
	}

	public void onDataUpdate()
	{
		adapter.notifyDataSetChanged();
		//Log.d("SlotListFragment.onDataUpdate", "SlotListFragment.onDataUpdate");
	}
}
