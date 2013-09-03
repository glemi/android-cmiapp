package ch.epfl.cmiapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ScheduleListHandler
	implements AdapterView.OnItemLongClickListener, ActionMode.Callback, 
	CmiReservation.BookingCallback, OnItemClickListener
{

	private ScheduleAdapter adapter = null;
	private ListView listView    = null;
	
	
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
	
}
