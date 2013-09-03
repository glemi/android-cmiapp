package ch.epfl.cmiapp;

import java.text.*;
import java.util.*;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.ScheduleManager.State;

public class ScheduleFragment extends Fragment 
	implements ScheduleManager.onStateChangedListener
{
	
	public interface ScheduleCallback
	{
		public void onScheduleChanged();
	}

	private ScheduleAdapter adapter = null;
	private Document cmiPage = null;
	private int dateOffset = 0;
	
	ScheduleManager scheduleManager;
	
	private String machId;
	
	private CmiReservation reservation = null;
	
	private ListView listView    = null;
	private TextView titleView   = null;
	private View     loadingView = null;
	private View	 noSlotsView = null;
	
	private ActionMode bookingMode = null;
	private int currentPosition = -1;
	
	
	public ScheduleFragment() 
	{
        this.dateOffset = 0;
        adapter = new ScheduleAdapter();
    }
	
	public ScheduleFragment(int dateOffset) 
	{
        this.dateOffset = dateOffset;
        adapter = new ScheduleAdapter();
        adapter.setDateOffset(dateOffset);
    }
	
	public void setArguments(Bundle arguments)
	{
		 machId = arguments.getString("MACHINE_ID");
		 adapter.setMachId(machId);
		 
		 if (arguments.containsKey("DATE_OFFSET"))
		 	dateOffset = arguments.getInt("DATE_OFFSET");
	}
	
	public void setDateOffset(int dateOffset)
	{
		this.dateOffset = dateOffset;
		adapter.setDateOffset(dateOffset);
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void setScrollPosition(int position, int offset)
	{
		//Log.d("ScheduleFragment.setScrollPosition", "syncing scroll position to: " + position + ":" + offset);
		listView.setSelectionFromTop(position, offset);
	}
	
	public void setPage(Document page)
	{
		cmiPage = page;
		adapter.setPage(page);
		adapter.setDisplayProgressIndicators(false);
		adapter.clearActionPending();
		updateEmptyView();
	}
	 
	public void viewUpdate(ViewGroup container)
	{
		updateEmptyView();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("DATE_OFFSET", dateOffset);
		outState.putString("MACHINE_ID", machId);
	}

	@Override
	public void onActivityCreated(Bundle state)
	{
		super.onActivityCreated(state);
		if(state != null)
		{
			machId     = state.getString("MACHINE_ID");
			dateOffset = state.getInt("DATE_OFFSET");
			
			adapter.setMachId(machId);
			adapter.setDateOffset(dateOffset);
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        View view   = inflater.inflate(R.layout.schedule_fragment, container);
        
        listView    = (ListView) view.findViewById(R.id.scheduleList);
        //titleView   = (TextView) view.findViewById(R.id.title);
    	loadingView = null;			 //view.findViewById(R.id.emptyLoading);        	
    	noSlotsView = null;			// view.findViewById(R.id.emptyNoSlots);
    	
    	listView.setAdapter(adapter);
    	//listView.setEmptyView(loadingView);
    	listView.setOnItemLongClickListener(scheduleManager);
    	listView.setOnItemClickListener(scheduleManager);
    	listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
    	
    	Activity activity = getActivity();
    	if (activity instanceof AbsListView.OnScrollListener)
    	{
    		AbsListView.OnScrollListener listener;
    		listener = (AbsListView.OnScrollListener) activity;
    		listView.setOnScrollListener(listener);
    	}
    	
    	Log.d("ScheduleFramgent.onCreateView", "onCreateView");
    	
    	//titleView.setText(getTitle());
        
        updateEmptyView();
        return view;
    }
    
    private void updateEmptyView()
    {
    	View container = this.getView();
    	if (container == null)
    		return;
    	/*
    	if (adapter == null || adapter.isWaitingForData())
    	{
        	listView.setEmptyView(loadingView);
        	loadingView.setVisibility(View.VISIBLE);
        	noSlotsView.setVisibility(View.INVISIBLE);
    	}
    	else if (adapter.getCount() == 0)
    	{
    		listView.setEmptyView(noSlotsView);
        	loadingView.setVisibility(View.INVISIBLE);
        	noSlotsView.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		loadingView.setVisibility(View.INVISIBLE);
        	noSlotsView.setVisibility(View.INVISIBLE);
    	}*/
    }
    
    public String getTitle()
    {
    	if (dateOffset != 0)
    	{
			DateFormat timeFormat = new SimpleDateFormat("EEEE MMM-dd");
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, dateOffset);
			return timeFormat.format(calendar.getTime());
    	}
    	else
    		return "Today";
    }
    

	public void onStateChanged(State newState)
	{
		switch (newState)
		{
		case WAITING_FOR_DATA:
			// TODO display empty view 
			break;
		case IDLE:
			adapter.setDisplayProgressIndicators(false);
			adapter.setActionHightlightEnabled(false);
			adapter.notifyDataSetChanged();
			break;
		case BOOKING_MODE:
			adapter.setActionHightlightEnabled(true);
			adapter.notifyDataSetChanged();
			break;
		case BOOKING_COMMIT:
			adapter.setDisplayProgressIndicators(true);
			adapter.setActionHightlightEnabled(false);
			adapter.notifyDataSetChanged();
			break;
		}
	}


}
