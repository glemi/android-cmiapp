package ch.epfl.cmiapp.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.cmiapp.core.Configuration;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.Inventory;
import ch.epfl.cmiapp.core.Schedule;
import ch.epfl.cmiapp.core.WebLoadedSchedule;
import ch.epfl.cmiapp.json.JsonStreamReader;
import ch.epfl.cmiapp.json.JsonableSchedule;
import android.content.Context;
import android.util.Log;
import android.content.AsyncTaskLoader;

/*
 * Instances, when data has to be reloaded:
 *  - data is out of date, i.e. expiration time is reached 
 *  - reservation has been posted
 *  - ui requests reload (schedule scrolled beyond limit)
 *  - requested configuration of a tool is changed
 * 
 * We will also take care of caching and locally storing the data until its
 * expiration time is reached. This should make the ui more responsive, 
 * allowing new data to be loaded while previously loaded data is already
 * displayed.
 * 
 * The question is who decides when the expiration of a dataset occurs and
 * where do we store this information.  
 * 
 * http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
 * 
 * There should be a instance of a ContentObserver, i.e. some derived class
 * that holds a reference to the Loader so it can call onContentChanged() 
 * on it. 
 * According to the API reference "checks to see if the loader is currently 
 * started; if so, it simply calls forceLoad(); otherwise, it sets a flag 
 * so that takeContentChanged() returns true."
 * 
 * We could create an inner class (non-static) that would automatically  
 * have access to the enclosing loader's methods. Instances of this class
 * would then be attached to e.g. the ScheduleManager to trigger the loading
 * of required data. 
 * 
 * Currently what we do is, in onBookingComplete() and changeConfiguration(),
 * we make the follwing call: 
 * loaderManager.restartLoader(0, null, this);
 * And yes, ScheduleManager holds a reference to the fragment's LoaderManager
 * which has always bugged me. 
 * 
 * What's important is to make sure that when a fragment gets destroyed and 
 * re-created, while it reconnects to the still existing loader, it also re-
 * creates the appropriate ContentObservers and connects them to the loader.  
 * Creating or reconnecting the loaders is done by calling
 * loaderManager.initLoader(id, args, callbacks);
 * 
 * If a new loader instance has to be created then the callback object will  
 * have its onCreateLoader(int id, Bundle args) getting called. The tricky
 * part is when that doesn't happen. 
 * In that case, after calling initLoader, we have to retrieve the loader
 * instance using loaderManager.getLoader(id). 
 * 
 * So we can do something along the lines of:
 * 
 * loaderManager.initLoader(SCHEDULE_LOADER, args, this);
 * ScheduleLoader loader = loaderManager.getLoader(SCHEDULE_LOADER);
 * ScheduleLoader.ScheduleObserver observer = loder.new ScheduleObserver(); 
 * 
 * The observer typically has an onChanged() method that has to be called
 * in order to notify it. Strictly speaking the observer is not observing
 * anything, its just sitting there waiting for someone to tell it it's 
 * observing something. 
 * 
 * Alternatively we could just keep a reference to the loader itself and 
 * call onContentChanged on the loader directly. 
 * 
 * Concerning the obnoxious reference to the LoaderManager inside
 * ScheduleManager we could get rid of that and use on the loader
 * 
 * registerListener(int id, OnLoadCompleteListener<D> listener)
 * 
 * i.e. we would have ScheduleManager implement the OnLoadCompleteListener
 * and say loader.registerListener(id, scheduleManager);
 * Whats puzzling is the meaning of the 'id' parameter here??
 * 
 * (Btw it would be also good to have some more inner classes for 
 * ScheduleManager so that it isn't so cluttered with public interfaces.)
 * 
 * Remember that the Loader should be instantiated by passing arguments
 * to it using a bundle.  
 * 
 * WHOA: really cool!
 * public void setUpdateThrottle (long delayMS). 
 * Can schedule automatic updates for an AsyncTaskLoader. 
 * >> EDIT: THAT'S WRONG! updateThrottle does not "schedule" new loads. Instead 
 *  it prevents new loads to be executed if the last load was done less 
 *  than delayMS milliseconds ago. It is a mechanism to prevent too frequent 
 *  reloading.  
 * 
 * When content has to be (re)loaded, the Loader has to be aware of 
 * what part of the content is still up-to date and what part needs
 * to be reloaded. We need a mapping on a day-by-day basis from schedule
 * data to a value that indicates whether the data is still valid or
 * has become invalid. 
 * 
 * When the observer reports a content change, it needs to specify 
 * the content of what days of the schedule have changed. 
 * 
 *  In principle it would be good to couple information like 
 *  	- valid/invalid
 *      - expiration time / time last updated
 *      - scheduled for reload 
 *      etc. 
 *  directly with the data, i.e. make it part of the Schedule's data 
 *  structure. Let's call it a status tag. 
 *  
 *  On the other hand, whenever schedule data will be in memory, the 
 *  loader will have a reference on it and yeah well... maybe not
 *  always... this is a bad assumption. or not?  
 *  
 *  So how about this: whenever the Observer reports data change, it 
 *  has to specify what date has changed. For example, when a 
 *  reservation was made, it reports on what day. If the user scrolls
 *  past the range of loaded days, it reports the following day as 
 *  invalidated. Even if it had not been loaded previously this gives
 *  a hint to the loader, which will always start loading the schedule
 *  on the day that was reported invalid. When the configuration has
 *  changed then I guess the entire schedule has to be reloaded. And 
 *  that should about cover it. 
 *    
 *  Now with setUpdateThrottle we can set the update interval to a 
 *  relatively short interval, but on every update triggered that way
 *  we will actually do a reload only if any data has be tagged as 
 *  invalid. Kewl. 
 *  
 *  Data will become invalid, when 
 *   - expiration date is reached
 *   - it has been invalidated by the observer
 *     
 *  The loader will find the first day of all invalidated days. And 
 *  schedule a reload of a chunk of the schedule starting on that day.
 *  It will mark all days that fall within the chunk as "scheduled 
 *  for reload". It will continue to schedule the loading of more 
 *  chunks until all days that were invalidated are also scheduled for 
 *  reload. Any chunk is identified by its date-offset.  
 *  
 *  A chunk is scheduled for download by storing its dateOffset in a 
 *  data structure (e.g. a stack or fifo). The loader will iterate
 *  over all dateOffsets (this should only very rarely be > 2) and 
 *  do the actual loading. 
 *  Once the data has arrived and been merged, the status tag gets 
 *  updated, with status 'valid' and the current time as time of the 
 *  last update.
 *  
 *  We could create a class "statusTag" with the corresponding fields. 
 *  Then we would need a mapping date -> statusTag. We could also 
 *  define an interface Updatable, with the method getStatusTag().
 *  
 *  Operations needed:
 *   - get status tag by date or dateoffset
 *   - get first date with status tag=invalid, i.e.
 *   - iterate over tags in sequential order, which means 
 *   - the mappings have to be sorted by date
 *   - iterate over elements of a chunk, i.e. the ability
 *   	to iterate over a specific range
 *    
 * 
 * Note about local reference to a context: 
 * 		(1) "Loaders may be used across multiple Activities (assuming 
 * 			they aren't bound to the LoaderManager), so NEVER hold a
 * 			reference to the context directly. Doing so will cause you 
 * 			to leak an entire Activity's context."
 *
 *		(2) "The superclass constructor will store a reference to the
 *		 	Application Context instead, and can be retrieved with a 
 *		 	call to getContext()."
 *		
 *      If storing the context is bad for us, why does this not also 
 *      apply for the superclass?
 *      
 *      >>> Because each application can have many Activities, and it 
 *      is possible to leak them if you aren't careful. Storing the 
 *      application context doesn't result in the possibility of a 
 *      memory leak because there is only one in the during the entire 
 *      application life cycle and it will always exist. Referencing 
 *      the application context instead of an Activity context is 
 *      usually a good way to avoid leaking an entire context in this 
 *      way.
 * 
 * Note on loading network data:
 *      Using a Loader to perform network requests isn't great practice,
 *      because (1) it means that your application will be hard on the 
 *      battery (having to poll for new data from the network repeatedly 
 *      each time you start the Activity, (2) there is no way to 
 *      observer the network for content changes without polling it 
 *      repeatedly, and (3) your application won't work offline.
 *
 *      So my answer is to forget about using the Loader/AsyncTask 
 *      combination entirely and to stick with a Service. The Service 
 *      can poll the network for data every once and a while and insert 
 *      new data into a ContentProvider. You can then use a CursorLoader 
 *      to load data from the ContentProvider without it needing to know 
 *      anything about where the data coming from.
 *      
 * From Android Developers: 
 *      Decide if you need a content provider. You need to build a content
 *      provider if you want to provide one or more of the following
 *      features:
 *        - You want to offer complex data or files to other applications.
 *        - ou want to allow users to copy complex data from your app
 *          into other apps.
 *        - You want to provide custom search suggestions using the search
 *          framework.
 *      You don't need a provider to use an SQLite database if the use is 
 *      entirely within your own application.
 */

public class CmiScheduleLoader extends AsyncTaskLoader<Schedule> 
{
	private CmiServerConnection server;
	private CmiAccount account;
	private Equipment equipment;
	
	private Schedule schedule;
	
	private Configuration config = null;
	
	private NavigableMap<LocalDate, DataItemStatus> dataStatus  = new TreeMap<LocalDate, DataItemStatus>();
	private boolean scheduleComplementaryLoad = false;
	
	private static final String TAG = "CmiScheduleLoader";
	
	public CmiScheduleLoader(Context context)
	{
		super(context);
		account = CmiAccount.getActive();
		server = account.getServerConnection();
		EquipmentManager.load(context);
		setDate(LocalDate.now());
	}
	
	public void setMachId(String machId)
	{
		Inventory inventory = EquipmentManager.getInventory();
		this.equipment = inventory.get(machId);
		//this.machId = machId;
	}
	
	public void setDate(LocalDate date)
	{
		setDateRange(date, date);
	}
	
	public void setDateRange(LocalDate dateStart, LocalDate dateEnd)
	{
		for (LocalDate date = dateStart; date.isBefore(dateEnd); date = date.plusDays(1))
		    dataStatus.put(date, new DataItemStatus(false));
	}
	
	public void setConfig(Configuration config)
	{
		this.config = config;
	}
	
	@Override
	public void deliverResult(Schedule newdata) 
	{
		if (schedule != null)
		{
			Log.v(TAG, "deliverResult: merging new schedule data with existing data");
			schedule = Schedule.merge(newdata, schedule);
		}
		else
			schedule = newdata;
		
		for (int index = 0; index < newdata.getDayCount(); index++)
		{
			LocalDate date = newdata.getDate(index);
			dataStatus.put(date, new DataItemStatus());
		}
		
		if (moreLoadsNeeded())
			forceLoad();
		
		super.deliverResult(schedule);
	}
	
	private boolean moreLoadsNeeded()
	{
		if (scheduleComplementaryLoad)
			return true;
		
		for (DataItemStatus status  : dataStatus.values())
			if (status.needsRelaod()) return true;
		
		return false;
	}
	
	private int getNextDateOffsetValue()
	{
		for (LocalDate date : dataStatus.keySet())
		{
			DataItemStatus status = dataStatus.get(date);
			if (status.needsRelaod())
				return Days.daysBetween(LocalDate.now(), date).getDays();
		}
		
		return 0;
	}
	
	@Override
	public Schedule loadInBackground()
	{
		Schedule newData;
		
		if (scheduleComplementaryLoad)
		{
			Log.v(TAG, "loadInBackground: initiating complementary load");
			newData = loadAllReservSchedule();
			scheduleComplementaryLoad = false;
		}
		else
		{
			int offset = getNextDateOffsetValue();
			Log.v(TAG, "loadInBackground: loading data at date offset = " + offset);
			newData = loadMainPageSchedule(offset);
			scheduleComplementaryLoad = (offset == 0);
			if (scheduleComplementaryLoad) 
				Log.v(TAG, "loadInBackground: scheduling complementary load"); 
		}
		// when this method returns while the Loader is in the started state
		// it will trigger a call of deliverResult() but deliverResult is 
		// is executed on the UI thread. 
		return newData;
	}

	@Override
	protected void onReset() 
	{
		onStopLoading();
		storeJsonData();
		schedule = null;
		dataStatus.clear();
	}

	private boolean storeJsonData()
	{
		Context context = super.getContext();
		String filename = "schedule." + equipment.getMachId() + ".json";
		JsonableSchedule jschedule = new JsonableSchedule(schedule);
		FileOutputStream outstream;
		JSONObject json;
		
		try {
			json = jschedule.serialize();
			outstream = context.openFileOutput(filename, Context.MODE_PRIVATE); 
			outstream.write(json.toString().getBytes());
			outstream.close();
			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	private boolean loadJsonData()
	{
		Context context = super.getContext();
		String filename = "schedule." + equipment.getMachId() + ".json";
		JsonableSchedule jschedule;
		JsonStreamReader reader;
		FileInputStream instream;
		JSONObject json;
		
		try {
			Log.v(TAG, "loadJsonData: attempting to load from file " + filename);
			instream = context.openFileInput(filename);
			reader = new JsonStreamReader(instream);
			json = reader.readJason();
			reader.close();
			jschedule = new JsonableSchedule(json, equipment);
			// Never call loadJsonData if more recent data has already been
			// lodaded. We will overwrite any existing schedule data here. 
			schedule = jschedule;
			Log.v(TAG, "loadJsonData: success");
			return true;
			
		} catch (FileNotFoundException e) {
			Log.v(TAG, "loadJsonData: file doesn't exist yet");
			e.printStackTrace();
		} catch (JSONException e) {
			Log.e(TAG, "loadJsonData: JSONException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "loadJsonData: IOException");
			e.printStackTrace();
		}	    
		return false;
	}
	
	@Override
	public void onCanceled(Schedule data) 
	{
		// TODO Auto-generated method stub
		super.onCanceled(data);
	}

	private Schedule loadMainPageSchedule(int dateOffset)
	{
		InputStream stream;
		Schedule schedule;
		
		if (equipment == null || account == null)
			return null;
		
		String machId = equipment.getMachId();
		Log.v(TAG, "loadMainPageSchedule: loading cmi main web page");
		try {
			if (config != null)
				stream = server.getMainPage(machId, config, dateOffset);
			else
				stream = server.getMainPage(machId, dateOffset);
			
			 schedule = new WebLoadedSchedule(stream, equipment);
		}
		catch (IOException exception) { return null; }
		
		return schedule;
	}
	
	private Schedule loadAllReservSchedule()
	{
		InputStream stream;
		Schedule schedule;
		
		if (equipment == null || account == null)
			return null;
		
		String machId = equipment.getMachId();
		try
		{
			stream = server.getAllReservationsPage(machId);
			schedule = new WebLoadedSchedule(stream, equipment);
		}
		catch (IOException exception) { return null; }
		
		return schedule;
	}
	
	
	public class ScheduleObserver 
	{
		public void invalidate(LocalDate date)
		{
			DataItemStatus status = dataStatus.get(date);	
			if (status == null)
				status = new DataItemStatus(false);
			dataStatus.put(date, status);
			// onContentChanged will trigger a forceLoad if the Loader is in 
			// the started state. We as the Observer are supposed to call 
			// it here. 
			CmiScheduleLoader.this.onContentChanged();
		}
		
		public void invalidate()
		{
			invalidate(LocalDate.now());
		}
		
	}
	
	private class DataItemStatus
	{
		public DataItemStatus() { this(true); }
		public DataItemStatus(boolean valid)
		{
			this.valid = valid;
			updated = LocalDateTime.now();
			expires = updated.plusHours(12); 
			// TODO: define duration from updated to expired 
		}
		public void update()
		{
			valid = true;
			updated = LocalDateTime.now();
			expires = updated.plusHours(12);
		}
		
		public LocalDateTime updated;
		public LocalDateTime expires;
		public boolean valid;
		
		public boolean needsRelaod()
		{
			return !valid || expires.isBefore(LocalDateTime.now());
		}
	}
	

	@Override
	protected void onStartLoading() 
	{
		if (schedule != null || loadJsonData())
		{   // Deliver any previously loaded data immediately.
			Log.v(TAG, "onStartLoading: delivering existing data immediately");
			deliverResult(schedule);
		}
		
		if (takeContentChanged() || schedule == null)
			forceLoad();
		
		/* calling forceLoad() on Loader which calls onForceLoad(), which is 
		 * overridden in AsyncTaskLoader and starting a new AsyncTask that will
		 * eventually invoke our loadInBackground() method.  
		 */
	}

	@Override
	protected void onStopLoading() 
	{
		/* call cancelLoad() on loader which calls onCancelLoad(), which is 
		 * overridden in AsynkTaskLoader and will either unschedule the loading 
		 * task or -- if it's already running -- call onCancelLoadInBackground()
		 * which does nothing unless we override it here. 
		 */
		cancelLoad(); // does nothing for now
	}


	
	
}
