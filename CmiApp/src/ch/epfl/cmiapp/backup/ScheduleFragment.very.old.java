package ch.epfl.cmiapp;

import java.text.*;
import java.util.*;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
//import android.widget.AdapterView.OnItemClickListener;

import org.jsoup.nodes.Document;

public class ScheduleFragment extends Fragment 
	implements AdapterView.OnItemLongClickListener, ActionMode.Callback, 
	CmiReservation.BookingCallback, OnItemClickListener
{
	
	public interface ScheduleCallback
	{
		public void onScheduleChanged();
	}

	private ScheduleAdapter adapter = null;
	private Document cmiPage = null;
	private int dateOffset = 0;
	
	private String machId;
	private String username;
	private String password;
	
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
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		SharedPreferences preferences = activity.getSharedPreferences("CMI_CREDENTIALS", Context.MODE_PRIVATE);
		username = preferences.getString("CMI_USERNAME", null);
		password = preferences.getString("CMI_PASSWORD", null);
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
        View view   = inflater.inflate(R.layout.schedule_fragment, container, false);
        
        listView    = (ListView) view.findViewById(R.id.scheduleList);
        //titleView   = (TextView) view.findViewById(R.id.title);
    	loadingView = 			 view.findViewById(R.id.emptyLoading);        	
    	noSlotsView = 			 view.findViewById(R.id.emptyNoSlots);
    	
    	listView.setAdapter(adapter);
    	listView.setEmptyView(loadingView);
    	listView.setOnItemLongClickListener(this);
    	listView.setOnItemClickListener(this);
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
    	}
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

    // from ActionMode.Callback
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) 
	{
		
		//adapter.notifyDataSetChanged();
		Log.d("ScheduleFragment", "onItemClick");
		return false;
	}

	// from ActionMode.Callback
	public boolean onCreateActionMode(ActionMode mode, Menu menu) 
	{
		Log.d("ScheduleFragment.onCreateActionMode", "creating an action mode");
		mode.setTitle("Make a Reservation");
		return true;
	}

	// from ActionMode.Callback
	public void onDestroyActionMode(ActionMode mode) 
	{
		currentPosition = -1;
		
		String message = reservation.report();
		if (!message.isEmpty())
		{
			Context context = getActivity();
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.show();
		}
		
		Log.d("ScheduleFragment.onDestroyActionMode", "reservation commit");
		reservation.commit();
		adapter.setDisplayProgressIndicators(true);
		adapter.setActionHightlightEnabled(false);
		adapter.notifyDataSetChanged();
		
		bookingMode = null;
	}

	// from ActionMode.Callback
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) 
	{
		return false;
	}

	// from AdapterView.OnItemLongClickListener
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (bookingMode == null)
		{
			CmiSlot slot = adapter.getItem(position);
			
			reservation = new CmiReservation(this);
			reservation.setCredentials(username, password);
			
			adapter.setActionHightlightEnabled(true);
			//listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			//listView.setItemChecked(position, true);
			itemStateChange(position);
			
			Activity activity = getActivity();
			bookingMode = activity.startActionMode(this);
			
			return true;
		}
		else return false;
		
	}

	// from CmiReservation.BookingCallback
	public void onBookingComplete()
	{
		CmiScheduleActivity activity = (CmiScheduleActivity) getActivity();
		if (activity != null)
		{
			activity.getLoaderManager().restartLoader(0, null, activity);
			adapter.setActionHightlightEnabled(false);
			adapter.notifyDataSetChanged();
		}
	}

	// from OnItemClickListener
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		//Log.d("ScheduleFragment.onItemClick", "Item Click... checked items: " + listView.getCheckedItemCount());
		if (bookingMode != null) 
			itemStateChange(position);
	}
	
	// from no interface, just an ordinary member function
	private void itemStateChange(int position)
	{
		CmiSlot slot = adapter.getItem(position);
		
		CmiSlot.BookingAction action = reservation.toogleBooking(slot);
		adapter.setActionPending(position, action);
		adapter.notifyDataSetChanged();
	}
    
}
