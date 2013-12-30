package ch.epfl.cmiapp.fragments;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.activities.CmiFragmentActivity;
import ch.epfl.cmiapp.adapters.ReservationListAdapter;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.CmiReservation;
import ch.epfl.cmiapp.core.CmiReservation.BookingCallback;
import ch.epfl.cmiapp.util.CmiLoader;
import ch.epfl.cmiapp.util.EquipmentManager;

import android.app.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.content.*;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.Fragment;

public class ReservationsFragment extends Fragment
	implements View.OnCreateContextMenuListener, LoaderManager.LoaderCallbacks<Document>, BookingCallback
{
	
	private ReservationListAdapter adapter;
	
	private ListView listView    = null;
	private TextView titleView   = null;
	private TextView noSlotsView = null;
	private View     loadingView = null;
	
	private boolean disableContextMenu = false;
	
	private static final int RESERVATIONS_PAGE_LOADER = 0;
	
	private static final int MENU_UNBOOK_ALL    = 0;
	private static final int MENU_GOTO_SCHEDULE = 1;
	
	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Context context = this.getActivity();
		adapter = new ReservationListAdapter(context);
		EquipmentManager.load(context);
	}
	
	@Override
	public void onAttach (Activity activity)
	{
		super.onAttach(activity);
		LoaderManager loaderManager = this.getLoaderManager();
        loaderManager.initLoader(RESERVATIONS_PAGE_LOADER, null, this);
        Log.d("ReservationsFragment.onAttach", "on attach");
	}
	
	@Override
	public void onStart()
	{
		Log.d("ReservationsFragment.onStart", "on start");
		
		LoaderManager loaderManager = this.getLoaderManager();
		Loader loader = loaderManager.getLoader(RESERVATIONS_PAGE_LOADER);
		loader.onContentChanged();
		getActivity().setProgressBarIndeterminateVisibility(true);
		super.onStart();
	}
	
	public void onResume() 
	{
		Log.d("ReservationsFragment.onResume", "on resume");
		super.onResume();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
	    View view   = inflater.inflate(R.layout.reservations_list, container, false);
	    
	    Log.d("ReservationsFragment.onCreateView", "on create view");
	    
	    listView    = (ListView) view.findViewById(R.id.reservationList);
	    //titleView   = (TextView) view.findViewById(R.id.title);
		noSlotsView = (TextView) view.findViewById(R.id.emptyNoSlots);
		loadingView = 			 view.findViewById(R.id.emptyLoading);
		
		noSlotsView.setText("No Reservations");
		//titleView.setText("Upcoming Reservations");
		listView.setAdapter(adapter);
		listView.setEmptyView(loadingView);
		
		this.registerForContextMenu(listView);
		
		updateEmptyView();
		
	    return view;
	}

	public void updateEmptyView()
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
	

	public Loader<Document> onCreateLoader(int id, Bundle args)
	{
		Context context = getActivity();
		CmiLoader loader = new CmiLoader(context, CmiLoader.PageType.USER_RESERVATIONS_PAGE);
		
		return loader;
	}

	public void onLoadFinished(Loader<Document> loader, Document document)
	{	
		adapter.setPage(document);
		updateEmptyView();
		
		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<Document> loader)
	{
		loader.forceLoad();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) 
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
	        	return super.onContextItemSelected(item);
	    }
	}
	
	public void unbookItem(int position)
	{
		CmiReservation reservation = adapter.getItem(position);
		
		Context context = getActivity();
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
		getActivity().setProgressBarIndeterminateVisibility(true);
	}
	
	public void gotoSchedule(int position)
	{
		CmiReservation reservation = adapter.getItem(position);
		String[] timeStamps = reservation.getSlotTimeStamps();
		Intent intent = new Intent(getActivity(), CmiFragmentActivity.class);
		intent.putExtra("CONTENT_TYPE", "SCHEDULE");
		intent.putExtra("MACHINE_ID", reservation.getMachId());
    	intent.putExtra("HIGHTLIGHT_SLOTS", timeStamps);
    	startActivity(intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		// TODO prevent double-unbook
		// ...before reservations page is reloaded (block context menu on this item)
		
		super.onCreateContextMenu(menu, v, menuInfo);

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
	}

	public void onBookingComplete()
	{
		this.getLoaderManager().restartLoader(RESERVATIONS_PAGE_LOADER, null, this);
		adapter.notifyDataSetChanged();
	}
	 
}
