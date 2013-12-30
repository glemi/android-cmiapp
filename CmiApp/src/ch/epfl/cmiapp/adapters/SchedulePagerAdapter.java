package ch.epfl.cmiapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import ch.epfl.cmiapp.util.ScheduleManager;
import ch.epfl.cmiapp.core.CmiSchedule;
import ch.epfl.cmiapp.fragments.SlotListFragment;


public class SchedulePagerAdapter extends FragmentPagerAdapter 
{
	private CmiSchedule schedule;
	private ScheduleManager scheduleManager;
	
	private boolean landscape;
	
	private int scrollPosition = 0;
	private int scrollOffset = 0;
	
	private String[] highlight;
    
	public SchedulePagerAdapter(FragmentManager fragmentManager, ScheduleManager scheduleManager)
	{
		super(fragmentManager);
		this.scheduleManager = scheduleManager;
		this.schedule = scheduleManager.getSchedule();
		
		int orientation = Resources.getSystem().getConfiguration().orientation;
		landscape = orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
	@Override
	public CharSequence getPageTitle(int position)
	{
		LocalDate date = schedule.getDateAt(position);
		LocalDate today = new LocalDate(); 
		
		int days = Days.daysBetween(today, date).getDays();
		switch (days)
		{
			case 0:  return "Today";
			case 1:  return "Tomorrow";
			default: return date.toString("EEEE MMM-dd"); 
		}
	}
	
	protected String getTag(int viewId, int index)
	{
		schedule.getDateAt(index);
		return "android:switcher:" + viewId + ":" + index;
	}    
	
	@Override
	public float getPageWidth(int position)
	{	
		int orientation = Resources.getSystem().getConfiguration().orientation;
		
		switch (orientation)
		{
		case Configuration.ORIENTATION_LANDSCAPE:
			return 0.5f;
		default:
			return 1.0f;
		}
	}

	@Override
	public int getCount() 
	{
		//Log.d("SchedulePagerAdapter", "getCount : " + schedule.getDayCount());
		return schedule.getDayCount();
	}

	@Override
	public Fragment createFragment(int position)
	{
		// Log.d("SchedulePagerAdapter.createFragment", "createFragment");
		// SchedulePageFragment fragment = new SchedulePageFragment();
		SlotListFragment fragment = new SlotListFragment();
		
		LocalDate date = scheduleManager.getSchedule().getDateAt(position);
		
	    Bundle arguments = new Bundle();
		arguments.putInt("POSITION", position);
		arguments.putString("MACHINE_ID", scheduleManager.getMachId());
		arguments.putSerializable("DATE", date);
		arguments.putBoolean("SHOW_TITLE", landscape);
		arguments.putStringArray("HIGHLIGHT_SLOTS", highlight);
		
		arguments.putSerializable("START_TIME", new LocalTime(00, 00));
		arguments.putSerializable("END_TIME",   new LocalTime(23, 59));
		
		fragment.setArguments(arguments);
		
		return fragment;
	}
	
	/*public void setScrollPosition(int scrollPosition, int offset)
	{
		this.scrollPosition = scrollPosition;
		this.scrollOffset = offset;
		
		for (Fragment fragment : this)
		{
			ScheduleFragment scheduleFragment = (ScheduleFragment) fragment;
			scheduleFragment.setScrollPosition(scrollPosition, offset);
		}
	}	*/
}
