package ch.epfl.cmiapp;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.CmiReservation.BookingCallback;
import ch.epfl.cmiapp.activities.CmiFragmentActivity;
import ch.epfl.cmiapp.adapters.ReservationListAdapter;
import ch.epfl.cmiapp.adapters.TransientAdapter;
import ch.epfl.cmiapp.adapters.TransientAdapter.TransientMode;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ReservationsListManager extends ListManager
	implements View.OnCreateContextMenuListener, BookingCallback
{
	private static final int MENU_UNBOOK_ALL    = 0;
	private static final int MENU_GOTO_SCHEDULE = 1;
	
	Context context;
	ReservationListAdapter adapter;
	TransientAdapter wrapper;
	
	private boolean disableContextMenu = false;
	
	public ReservationsListManager()
	{
		adapter = new ReservationListAdapter();
		wrapper = new TransientAdapter(adapter);
		
		wrapper.setEmptyMessage("no reservations.");
		wrapper.setLoadingMessage("loading...");
		wrapper.setFailedMessage("Unable to fetch data from CMi server.");
		wrapper.setMode(TransientAdapter.TransientMode.EMPTY);
	}

	public ReservationsListManager(AbsListView list)
	{	
		this();
		super.attachList(list);
	}
	
	@Override
	public int getLoaderId()
	{
		return CmiLoader.PageType.USER_RESERVATIONS_PAGE.toInt();
	}
	
	@Override
	public void onAttachList(AbsListView list)
	{
		context = list.getContext();
		//list.setOnCreateContextMenuListener(this);
	}
	
	@Override
	public BaseAdapter getAdapter() { return wrapper; }
	public String getTitle() { return "Upcoming Reservations"; }

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		CmiReservation reservation = adapter.getItem(info.position);
		
		int slotCount = reservation.getSlotCount(); 
		
		String unbookText;
		switch(slotCount)
		{
			case 1:  unbookText = "Unbook this slot"; break;
			case 2:  unbookText = "Unbook both slots"; break;
			default: unbookText = "Unbook all " + slotCount + " slots";
		}
		
	    menu.setHeaderTitle("Change Reservation");
	    menu.add(Menu.NONE, MENU_UNBOOK_ALL, 0, unbookText);
	    menu.add(Menu.NONE, MENU_GOTO_SCHEDULE, 1, "Go to schedule...");
	    
	    super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		
	    switch (item.getItemId()) 
	    {
	        case MENU_UNBOOK_ALL:     
	        	unbookItem(info.position);
	        	return true;
	        case MENU_GOTO_SCHEDULE:	
	        	gotoSchedule(info.position);
	        	return true;
	        default:   
	        	return false;
	    }
	}
	
	public void gotoSchedule(int position)
	{
		CmiReservation reservation = adapter.getItem(position);
		String[] timeStamps = reservation.getSlotTimeStamps();
		Intent intent = new Intent(context, CmiFragmentActivity.class);
		intent.putExtra("CONTENT_TYPE", "SCHEDULE");
		intent.putExtra("MACHINE_ID", reservation.getMachId());
    	intent.putExtra("HIGHTLIGHT_SLOTS", timeStamps);
    	
    	context.startActivity(intent);
	}
	
	public void unbookItem(int position)
	{
		CmiReservation reservation = adapter.getItem(position);
		
		SharedPreferences preferences = context.getSharedPreferences("CMI_CREDENTIALS", Context.MODE_PRIVATE);
		String username = preferences.getString("CMI_USERNAME", null);
		String password = preferences.getString("CMI_PASSWORD", null);
		
		reservation.setCredentials(username, password);
		reservation.setBookingCallback(this);
    	reservation.unbookAll();
    	reservation.commit();
    	disableContextMenu = true;
    	
		String message = reservation.report() + "... please wait";
		if (!message.isEmpty())
		{
			Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
			toast.show();
		}
		
		//getActivity().setProgressBarVisibility(true);
		//getActivity().setProgressBarIndeterminateVisibility(true);
	}
	
	@Override
	public Loader<Document> onCreateLoader(int id, Bundle bundle)
	{
		wrapper.setMode(TransientMode.LOADING);
		return new CmiLoader(context, CmiLoader.PageType.USER_RESERVATIONS_PAGE);
	}
	
	@Override
	public void onLoadFinished(Loader<Document> loader, Document page)
	{
		if (!CmiEquipment.isEquipmentListLoaded())
			CmiEquipment.loadEquipmentList(context);
		Log.d("ReservationsListManager.onLoadFinished", "Reservations Finished Loading.");
		
		if (page != null)
			wrapper.setMode(TransientAdapter.TransientMode.EMPTY);
		else
			wrapper.setMode(TransientAdapter.TransientMode.FAILED);
		
		adapter.setPage(page);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> list, View view, int position, long itemId)
	{
		//list.showContextMenuForChild(view);
		return false;
	}

	public void onBookingComplete()
	{
		wrapper.notifyDataSetChanged();
	}
	
}
