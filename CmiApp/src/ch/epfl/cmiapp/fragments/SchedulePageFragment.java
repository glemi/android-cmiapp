package ch.epfl.cmiapp.fragments;

import org.joda.time.*;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.ScheduleManager;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;

import android.support.v4.app.*;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class SchedulePageFragment extends Fragment
{
	private Bundle arguments;
	
	private ScheduleManager scheduleManager = null;
	private LocalDate date = new LocalDate();
	private String machId;
	private int position;

	private int orientation = Configuration.ORIENTATION_PORTRAIT;
	
	private SlotListFragment leftFragment = null;
	private SlotListFragment rightFragment = null;
	private SlotListFragment verticalFragment = null;
	
	
	public void setScheduleManager(ScheduleManager scheduleManager)
	{
		this.scheduleManager = scheduleManager;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//Log.d("SchedulePageFragment.onCreate", "SchedulePageFragment");
		if (savedInstanceState != null)
			arguments = savedInstanceState;
		else
			arguments = this.getArguments();
		
		readArguments(arguments);

		Fragment fragment = this.getActivity().getSupportFragmentManager().findFragmentByTag("SCHEDULE");
		if (fragment != null && fragment instanceof ScheduleMasterFragment)
		{
			//Log.d("SchedulePageFragment.onCreate", "master schedule fragment found!");
			ScheduleMasterFragment master = (ScheduleMasterFragment) fragment;
			this.scheduleManager = master.getScheduleManager();
		}
		else
		{
			throw new RuntimeException("SchedulePageFragment must be " +
					"the child of a ScheduleMasterFragment with Tag set to 'schedule'");
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putAll(arguments);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//Log.d("SchedulePageFragment.onCreateView", "ContainerId = " + container.getId());
		
		View view = inflater.inflate(R.layout.schedule_page, container, false);
		
		FragmentManager fragmentManager = this.getChildFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		
		TextView textView = (TextView) view.findViewById(R.id.debugText);
		textView.setText(Integer.toString(position));
		
		orientation = Configuration.ORIENTATION_PORTRAIT; //getResources().getConfiguration().orientation;
		switch (orientation)
		{
			case Configuration.ORIENTATION_LANDSCAPE:
				String tagLeft  = "ScheduleFragment:" + machId + ':' + date.toString() + "left";
				String tagRight = "ScheduleFragment:" + machId + ':' + date.toString() + "right";
				
				leftFragment  = (SlotListFragment) fragmentManager.findFragmentByTag(tagLeft);
				rightFragment = (SlotListFragment) fragmentManager.findFragmentByTag(tagRight);
				
				if (leftFragment == null)
				{
					leftFragment = new SlotListFragment();
					Bundle args  = new Bundle(this.arguments); // copy own arguments
					args.putSerializable("START_TIME", new LocalTime(00, 00));
					args.putSerializable("END_TIME",   new LocalTime(12, 59));
					leftFragment.setArguments(args);
					transaction.add(R.id.frameLeft,  leftFragment, tagLeft);
				}
				else transaction.attach(leftFragment);
				
				if (rightFragment == null)
				{
					rightFragment = new SlotListFragment();
					Bundle args  = new Bundle(this.arguments); // copy own arguments
					args.putSerializable("START_TIME", new LocalTime(13, 00));
					args.putSerializable("END_TIME",   new LocalTime(23, 59));
					rightFragment.setArguments(args);
					transaction.add(R.id.frameRight, rightFragment, tagRight);
				}
				else transaction.attach(rightFragment);
				
				
				verticalFragment = null;
				break;
				
			case Configuration.ORIENTATION_PORTRAIT:
				
				String tag = "ScheduleFragment:" + machId + ':' + date.toString();
				verticalFragment = (SlotListFragment) fragmentManager.findFragmentByTag(tag);
				
				if (verticalFragment == null)
				{
					verticalFragment = new SlotListFragment();
					Bundle args  = new Bundle(this.arguments); // copy own arguments
					args.putSerializable("START_TIME", new LocalTime(00, 00));
					args.putSerializable("END_TIME",   new LocalTime(23, 59));
					verticalFragment.setArguments(args);
					transaction.add(R.id.frame, verticalFragment, tag);
				}
				else transaction.attach(verticalFragment);
				
				orientation = getResources().getConfiguration().orientation;
				verticalFragment.setShowTitle(orientation == Configuration.ORIENTATION_LANDSCAPE);
				
				
				leftFragment 	= null;
				rightFragment 	= null;
				break;
		}
		
		// http://stackoverflow.com/questions/12276243/commit-fragment-from-onloadfinished-within-activity
		transaction.commitAllowingStateLoss();
		return view;
	}
	
	private void readArguments(Bundle args)
	{
		date = (LocalDate) args.getSerializable("DATE");
		machId = args.getString("MACHINE_ID");
		position = args.getInt("POSITION");
	}
	
}
