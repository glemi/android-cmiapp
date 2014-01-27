package ch.epfl.cmiapp.util;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.adapters.SlotListAdapter;
import ch.epfl.cmiapp.core.CmiReservation;
import ch.epfl.cmiapp.core.CmiSchedule;
import ch.epfl.cmiapp.core.CmiSlot;
import ch.epfl.cmiapp.core.Configuration;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.util.CmiLoader.PageType;

import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.support.v4.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


// http://stackoverflow.com/questions/157856/do-java-listeners-need-to-be-removed-in-general
// http://stackoverflow.com/questions/8465258/how-can-i-force-the-action-bar-to-be-at-the-bottom-in-ics
// http://android-developers.blogspot.com/2011/04/customizing-action-bar.html

public class ScheduleManager
	implements AdapterView.OnItemLongClickListener, OnItemClickListener, ActionMode.Callback,
	CmiReservation.BookingCallback, LoaderManager.LoaderCallbacks<Document>
{
	private Context			context;
	private LoaderManager	loaderManager;
	
	private String			machId;
	private Equipment		eqpt;
	
	private CmiSchedule		schedule;
	private CmiReservation	reservation;
	private State			state;
	
	private final static int LOADER_ID_TABLE = 0;
	private final static int LOADER_ID_TODAYRES = 1;
	
	//TODO these should be WEAK references;
	private Set<SlotListAdapter> adapters = new HashSet<SlotListAdapter>();
	// Set<onStateChangedListener> onStateChangedListeners = new
	// HashSet<onStateChangedListener>();
	ListenerSet<onStateChangedListener>	onStateChangedListeners	= new ListenerSet<onStateChangedListener>();
	
	private boolean disallowReservations = false;
	
	
	
	public enum State
	{
		WAITING_FOR_DATA, IDLE, BOOKING_MODE, BOOKING_COMMIT, WAITING_FOR_RELOAD
	}
	
	public interface onStateChangedListener
	{
		public abstract void onStateChanged(State newState);
		public abstract void onDataUpdate();
	}
	
	public interface onScheduleLoadedListener
	{
		public abstract void onScheduleLoaded();
	}
	
	public ScheduleManager(Context context, LoaderManager loaderManager, Equipment equipment)
	{
		this.machId = equipment.getMachId();
		this.context = context;
		this.loaderManager = loaderManager;
		
		this.eqpt = equipment;
		
		Bundle arguments = new Bundle();
		arguments.putInt("DATE_OFFSET", 0);
		loaderManager.initLoader(LOADER_ID_TABLE, arguments, this);
		loaderManager.initLoader(LOADER_ID_TODAYRES, null, this);
		
		SharedPreferences preferences = context.getSharedPreferences("CMI_CREDENTIALS", Context.MODE_PRIVATE);
		String userName = preferences.getString("CMI_USERNAME", null);
		
		this.schedule = new CmiSchedule(equipment, userName);
	}
	
	public SlotListAdapter getAdapter(LocalDateTime start, LocalDateTime end)
	{
		SlotListAdapter adapter = new SlotListAdapter();
		adapter.setContent(schedule, start, end);
		adapters.add(adapter);
		return adapter;
	}
	
	public void releaseAdapter(SlotListAdapter adapter)
	{
		adapters.remove(adapter);
	}
	
	public void highlightSlots(String[] timeStamps)
	{
		for (SlotListAdapter adapter : adapters)
			adapter.highlightSlots(timeStamps);
	}
	
	public void changeConfiguration(Configuration newConfig)
	{
		Log.d("ScheduleManager.changeConfiguration", "new Configuration: " + newConfig.toString());
		
		changeState(State.WAITING_FOR_RELOAD);
		loaderManager.restartLoader(0, null, this);
		//loaderManager.
		// TODO: restart all loaders + change config
	}
	
	public void displayConfiguration()
	{
		if (eqpt.isConfigurable())
		{
				String text = "Current Configuration:\n\n";
			
			for (Configuration.Setting setting : eqpt.getConfig())
			{
				if (!setting.getsDisplayed()) continue;
				text += setting.getTitle() + ":";
				text += setting.getTitle().length() > 20 ? "\n\t" : "\t";
				text += setting.getCurrent().title + "\n";			
			}
						
			Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	public State getState() 		 { return this.state; }
	public String getMachId() 		 { return this.machId; }
	public CmiSchedule getSchedule() { return this.schedule; }
	
	public void registerOnStateChangedListener(onStateChangedListener listener)
	{
		this.onStateChangedListeners.add(listener);
	}
	
	public void onBookingComplete()
	{
		changeState(State.WAITING_FOR_RELOAD);
		loaderManager.restartLoader(0, null, this);
	}
	
	public boolean onActionItemClicked(ActionMode mode, MenuItem item)
	{
		switch (item.getItemId()) 
		{
        case R.id.bookingCommit:
        	mode.finish();
        	return true;
        case R.id.bookingCancel:
        	this.reservation.clear();
            mode.finish();
            return true;
        default:
            return false;
		}
	}
	
	public boolean onCreateActionMode(ActionMode mode, Menu menu)
	{
		mode.setTitle("Make a Reservation");
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.booking_action_menu, menu);
		return true;
	}
	
	public void onDestroyActionMode(ActionMode mode)
	{
		if (reservation.getSlotCount() > 0)
		{
			reservation.commit();
			changeState(State.BOOKING_COMMIT);
			/* Listeners should: - display progress indicators to indicate the wait
			 * for data - disable action highlight on listitems that are being
			 * (un)booked - notify their listviews that dataset has changed
			 */
			String message = reservation.report();
			if (message.length() > 0)
			{
				Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
				toast.show();
			}
		}
		else changeState(State.IDLE);
	}
	
	public boolean onPrepareActionMode(ActionMode mode, Menu menu)
	{
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (!parent.isClickable()) return;
		Log.d("ScheduleManager.onItemClick", "ScheduleManager.onItemClick");
		if (state == State.BOOKING_MODE)
		{
			SlotListAdapter adapter = (SlotListAdapter) parent.getAdapter();
			
			CmiSlot slot = adapter.getSlot(position);
			CmiSlot.BookingAction action = reservation.toogleBooking(slot);
			Log.d("ScheduleManager.onItemClick", "BOOKING MODE ON: [" + action.toString() +  "] on " + slot.getStartTime().toString("EEEE, hh:mm"));
			
			adapter.setActionPending(position, action);
			adapter.notifyDataSetChanged();
		}
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (disallowReservations) 
		{
			String text = "Reservations are not currently possible on this machine.";
			Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
			toast.show();
			return false;
		}
		if (!parent.isClickable()) return false;
		//Log.d("ScheduleManager.onItemLongClick", "ScheduleManager.onItemLongClick State=" + state.toString());
		
		if (state == State.IDLE)
		{
			if (eqpt.isConfigurable() && !eqpt.getConfig().isValid())
			{
				String message = "You need to configure this tool before you can make a reservation.";
				Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				return true;
			}
			
			SharedPreferences data = parent.getContext().getSharedPreferences("CMI_CREDENTIALS", Context.MODE_PRIVATE);
			String username = data.getString("CMI_USERNAME", null);
			String password = data.getString("CMI_PASSWORD", null);
			
			SlotListAdapter adapter = (SlotListAdapter) parent.getAdapter();
			
			reservation = new CmiReservation(this);
			reservation.setCredentials(username, password);
			
			CmiSlot slot = adapter.getSlot(position);
			CmiSlot.BookingAction action = reservation.toogleBooking(slot);
			
			Log.d("ScheduleManager.onItemClick", "SWITCHING ON BOOKING MODE: Booking 1 slot - " + slot.getStartTime().toString("EEEE, hh:mm"));
			
			adapter.setActionHightlightEnabled(true);
			adapter.setActionPending(position, action);
			adapter.notifyDataSetChanged();
			
			changeState(State.BOOKING_MODE);
			
			/*
			 * Listeners should - create an Action mode -
			 * setActionHightlightEnabled(true)
			 */
			return true;
		}
		else
			return false;
	}
	
	private void changeState(State newState)
	{
		this.state = newState;
		Log.d("ScheduleManager.changeState", "new state: " + newState.toString() + "\t listeners: " + onStateChangedListeners.count());
		
		for (SlotListAdapter adapter : adapters) switch (newState)
		{
		case WAITING_FOR_DATA:
			break;
		case IDLE:
			adapter.setActionHightlightEnabled(false);
			adapter.setDisplayProgressIndicators(false);
			adapter.notifyDataSetChanged();
			Log.d("ScheduleManager.changeState", "back to idle.");
			break;
		case BOOKING_MODE:
			adapter.setActionHightlightEnabled(true);
			adapter.notifyDataSetChanged();
			break;
		case BOOKING_COMMIT:
			adapter.setActionHightlightEnabled(false);
			adapter.setDisplayProgressIndicators(true);
			adapter.notifyDataSetChanged();
			break;
		case WAITING_FOR_RELOAD:
			
		}
		
		for (onStateChangedListener listener : onStateChangedListeners)
			listener.onStateChanged(newState);
	}
	
	private void checkReservationsOk()
	{
		if (!disallowReservations && this.eqpt.isLocked())
		{
			disallowReservations = true;
			
			String text = "This tool's configuration parameters are out of date.";
			text += "No reservations can be made. Please request an update with ";
			text += "the developer.";
			Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
			toast.show();
		}
	}
	
	public Loader<Document> onCreateLoader(int id, Bundle args)
	{
		LocalDate startDate = new LocalDate();
		if (args != null)
		{
			int dateOffset = args.getInt("DATE_OFFSET", 0);
			startDate.plusDays(dateOffset);
		}
		Log.d("ScheduleManager", "onCreateLoader");
		
		CmiLoader cmiLoader = null;
		switch (id)
		{
		case LOADER_ID_TABLE:
			if (eqpt.isConfigurable())			
			{
				cmiLoader = new CmiLoader(context, PageType.MAIN_PAGE_CONFIG_RES);
				cmiLoader.setDate(startDate);
				cmiLoader.setMachId(machId);
				cmiLoader.setConfig(eqpt.getConfig());
			}
			else
			{
				cmiLoader = new CmiLoader(context, PageType.MAIN_PAGE_RES);
				cmiLoader.setDate(startDate);
				cmiLoader.setMachId(machId);
			}			
			break;
		case LOADER_ID_TODAYRES:
			cmiLoader = new CmiLoader(context, PageType.ALL_RESERVATIONS_PAGE);
			cmiLoader.setDate(new LocalDate()); // today. always.
			cmiLoader.setMachId(machId);
		}
		return cmiLoader;
	}
	
	public void onLoadFinished(Loader<Document> loader, Document document)
	{
		Log.d("ScheduleManager", "onLoadFinished");
		
		if (document != null)
		{
			switch (loader.getId())
			{
			case LOADER_ID_TABLE: // the standard page with reservations table
				schedule.parseDocument(document, PageType.MAIN_PAGE_RES);
				checkReservationsOk();
				changeState(State.IDLE);
				// huh ? the following line seems to have worked up to now, 
				// suddenly starts giving illegalstateexceptions
				// TODO find out why IllegalStateException thrown here
				// loaderManager.getLoader(LOADER_ID_TODAYRES).onContentChanged();
				break;
			case LOADER_ID_TODAYRES: // today's reservations -- to complete the data
				schedule.parseDocument(document, PageType.ALL_RESERVATIONS_PAGE);
				break;
			}	
			
			for (onStateChangedListener listener : onStateChangedListeners)
				listener.onDataUpdate();
			
			// TODO count number of ongoing (re)loads
			// then change state only when all re finished
		}
	}
	
	public void onLoaderReset(Loader<Document> arg0)
	{
		Log.d("ScheduleManager", "onLoaderReset");
		// TODO ScheduleManager.onLoaderReset
	}
	
	/*
	 * private void itemStateChange(int position) { CmiSlot slot =
	 * adapter.getItem(position); CmiSlot.BookingAction action =
	 * reservation.toogleBooking(slot); adapter.setActionPending(position,
	 * action); adapter.notifyDataSetChanged(); }
	 */
}
