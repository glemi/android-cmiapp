package ch.epfl.cmiapp.fragments;

import ch.epfl.cmiapp.CmiEquipment;
import ch.epfl.cmiapp.ListScrollSyncer;
import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.ScheduleManager;
import ch.epfl.cmiapp.CmiEquipment.Configuration;
import ch.epfl.cmiapp.CmiEquipment.Configuration.*;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;
import ch.epfl.cmiapp.R.menu;
import ch.epfl.cmiapp.ScheduleManager.State;
import ch.epfl.cmiapp.ScheduleManager.onStateChangedListener;
import ch.epfl.cmiapp.adapters.SchedulePagerAdapter;

import android.support.v4.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class ScheduleMasterFragment extends Fragment 
	implements onStateChangedListener, ConfigDialogFragment.Callbacks, OnDismissListener
{
	private Bundle arguments;
	
	private enum Status {LOADING, FAILED_LOADING, NORMAL, RELOADING}
	
	private ListScrollSyncer listScrollSyncer = new ListScrollSyncer();
	private ScheduleManager scheduleManager = null;
	private SchedulePagerAdapter adapter = null;
	private ViewPager pager = null;
	private ActionMode actionMode;
	
	private TextView emptyView;
	
	private String machId;
	private String eqptName;
	private CmiEquipment equipment;
	
	@Override
	public void onStart()
	{	
		// TODO: display toast with current configuration.
		scheduleManager.displayConfiguration();
		super.onStart();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d("ScheduleMasterFragment", "**********************onCreate");
		if (savedInstanceState != null)
			arguments = savedInstanceState;
		else
			arguments = this.getArguments();
		
		this.machId = arguments.getString("MACHINE_ID");
		this.eqptName = arguments.getString("EQUIPMENT_NAME");
		this.equipment = CmiEquipment.getEquipmentByMachId(machId);
		this.getActivity().setTitle(eqptName);
		this.setHasOptionsMenu(true);
		
		LoaderManager loaderManager = this.getLoaderManager();
		scheduleManager = new ScheduleManager(getActivity(), loaderManager, machId);
		scheduleManager.registerOnStateChangedListener(this);
		
		FragmentManager fragmentManager = this.getFragmentManager();
		adapter = new SchedulePagerAdapter(fragmentManager, scheduleManager);
		
		super.onCreate(savedInstanceState);
		Log.d("ScheduleMasterFragment", "**********************END onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.d("ScheduleMasterFragment", "onCreateView; tag=" + this.getTag());
		View view = inflater.inflate(R.layout.activity_schedule, container, false);
		
		emptyView = (TextView) view.findViewById(R.id.emptyView);
		
		pager = (ViewPager) view.findViewById(R.id.schedulePager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(4);
        
        changeStatus(Status.LOADING);

		return view;
	}

	public void onStateChanged(State newState)
	{
		switch (newState)
		{
		case IDLE:
			adapter.notifyDataSetChanged();
			changeStatus(Status.NORMAL);
			centerViewVertically();			
			
			break;
		case BOOKING_MODE:
			actionMode = getActivity().startActionMode(scheduleManager);
			break;
		case BOOKING_COMMIT:
			actionMode = null;
			break;
		case WAITING_FOR_RELOAD:
			changeStatus(Status.RELOADING);
		}
	}
	
	public void hightLightSlots(String[] timeStamps)
	{
		if (timeStamps.length == 0) return;
		int position = this.scheduleManager.getSchedule().getPositionOf(timeStamps[0]);
		pager.setCurrentItem(position, true);
	}
	
	public boolean centerViewVertically()
  	{
  		if (adapter.getCount() == 0 || !this.getUserVisibleHint())
  			return false;
  		
  		int position = 0;
  		
		if (arguments.containsKey("HIGHTLIGHT_SLOTS"))
		{
			String[] timeStamps = arguments.getStringArray("HIGHTLIGHT_SLOTS");
			arguments.remove("HIGHTLIGHT_SLOTS");
			this.highlightSlots(timeStamps);
			
			int index = timeStamps.length / 2;
			position = scheduleManager.getSchedule().getSlotPosition(timeStamps[index]);
		}
		else
		{
			position = scheduleManager.getSchedule().getNowSlotPosition();
			
			if (position < 0) // no slot at this time... just center the view
				position = scheduleManager.getSchedule().getSlotsPerDay() / 2;
		}
  		
  		int height = this.getActivity().getWindowManager().getDefaultDisplay().getHeight();
  		int offset = (int) Math.round(height*0.35);
  		
  		listScrollSyncer.setScrollPosition(position, offset);
  		
  		Log.d("SlotListFragment.centerViewVertically", 
  			String.format("adapter: %d items, center item: %d, listView Hieght: %d, computed Offset: %d", adapter.getCount(), position, height, offset));
  		return true;
  	}
	
	public void highlightSlots(String[] timeStamps)
    {
    	if(timeStamps.length == 0) return;
    	String timeStamp = timeStamps[0];
    	
    	int position = scheduleManager.getSchedule().getPositionOf(timeStamp);
    	
    	if (position >= 0)
    	{
    		pager.setCurrentItem(position, true);
    		scheduleManager.highlightSlots(timeStamps);
    		// scheduleManager.highlightSlots(...);
    	}
    }
	

	public ScheduleManager getScheduleManager()
	{
		return this.scheduleManager;
	}

	public ListScrollSyncer getListScrollSyncer()
	{
		return this.listScrollSyncer;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		// saved state recovered by onCreate and onCreateView;
		outState.putAll(arguments);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{	
		if (equipment.isConfigurable)
			inflater.inflate(R.menu.eqpt_config_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
    	 switch (item.getItemId()) 
    	 {
         case R.id.config:
        	 ConfigDialogFragment dialog = new ConfigDialogFragment();
        	 dialog.setEquipment(equipment);
        	 dialog.setListener(this);
        	 dialog.show(this.getFragmentManager(), "configure " + eqptName);
             return true;
         default:
             return super.onOptionsItemSelected(item);
    	 }
	}
	
	public void changeStatus(Status status)
	{
		switch (status)
		{
		case NORMAL:
			getActivity().setProgressBarIndeterminateVisibility(false);
			emptyView.setVisibility(View.INVISIBLE);
			break;
		case FAILED_LOADING:
			getActivity().setProgressBarIndeterminateVisibility(false);
			emptyView.setText("Unable to fetch data from CMI server.");
			break;
		case LOADING:
			getActivity().setProgressBarIndeterminateVisibility(true);
			emptyView.setVisibility(View.VISIBLE);
			emptyView.setText("loading...");
		case RELOADING:
			getActivity().setProgressBarIndeterminateVisibility(true);
			emptyView.setVisibility(View.INVISIBLE);
			break;
		}
	}

	public void onDataUpdate()
	{
		adapter.notifyDataSetChanged();
		
	}

	public void onConfigChange(Configuration newConfig)
	{
		scheduleManager.changeConfiguration(newConfig);		
	}

	public void onConfigCancel()
	{
		// TODO Auto-generated method stub
		
	}

	public void onDismiss(DialogInterface dialog)
	{
		// TODO Auto-generated method stub
		Log.d("ScheduleMasterFragment.OnDismiss", "dismissed!");
	}
}
