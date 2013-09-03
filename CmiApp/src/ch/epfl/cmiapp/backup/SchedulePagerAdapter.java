package ch.epfl.cmiapp;

import android.support.v4.view.PagerAdapter;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.*;

import java.util.HashMap;
import java.util.Iterator;

import org.jsoup.nodes.Document;


public class SchedulePagerAdapter extends PagerAdapter 
	implements Iterable<ScheduleFragment>
{
	
	private static final int CHUNK_SIZE = 11;
	private Document cmiPage = null;
	
	private int scrollPosition = 0;
	private int scrollOffset = 0;
	
	private final FragmentManager manager;
    private FragmentTransaction transaction = null;
    @SuppressLint("UseSparseArrays")
	private HashMap<Integer, ScheduleFragment> activeFragments = new HashMap<Integer, ScheduleFragment>();
    
    private Bundle arguments;
    
	public SchedulePagerAdapter(FragmentManager manager)
	{
		this.manager = manager;
	}
	
	@Override
	public CharSequence getPageTitle(int position)
	{
		if (activeFragments.containsKey(position))
		{
			ScheduleFragment fragment = activeFragments.get(position);
			return fragment.getTitle();
		}
		else
		{
			if(position == 0)
				return "Today";
			else
				return "Tomorrow";
		}
	}

	public void setArguments(Bundle arguments)
	{
		this.arguments = arguments;
	}

	public void setPage(Document document)
	{
		this.cmiPage = document;
		
		for (ScheduleFragment fragment : this)
			fragment.setPage(document);
	}
	
	public void setScrollPosition(int scrollPosition, int offset)
	{
		this.scrollPosition = scrollPosition;
		this.scrollOffset = offset;
		
		for (ScheduleFragment fragment : this)
			fragment.setScrollPosition(scrollPosition, offset);
	}

	public ScheduleFragment getItem(int position)
	{
		// usually there are 3 fragments active: the currently displayed one
		// plus one on the left and right. This is determined by 
		// ViewPager::setOffscreenPageLimit(int limit)
		return activeFragments.get(position);
	}
	
	@Override
	public int getCount() 
	{
		return CHUNK_SIZE;
	}
	
	public Object instantiateItem(View container, int position) 
	{	
        if (transaction == null) 
            transaction = manager.beginTransaction();

        Log.d("SchedulePagerAdapter.instantiateItem", "container ID = " + container.getId());
        // Do we already have this fragment?
        String tag = makeFragmentName(container.getId(), position);
        ScheduleFragment fragment = (ScheduleFragment) manager.findFragmentByTag(tag);
        
        if (fragment != null) 
            transaction.attach(fragment);
        else 
        {
    		fragment = new ScheduleFragment(position);
    		arguments.putInt("DATE_OFFSET", position);
    		fragment.setArguments(arguments);

            transaction.add(container.getId(), fragment, tag);
        }

        fragment.setPage(cmiPage);
        activeFragments.put(position, fragment);
        
        return fragment;
    }
	
	 @Override
	 public void destroyItem(View container, int position, Object object)
	 {
		 if (transaction == null) 
			 transaction = manager.beginTransaction();
	        
		 transaction.detach((Fragment)object);
		 activeFragments.remove(position);
	 }

	@Override
    public void startUpdate(View container) 
	{
		ScheduleFragment fragment = (ScheduleFragment) manager.findFragmentById(container.getId());
		if (fragment != null)
		{
			fragment.viewUpdate((ViewGroup) container);
			fragment.setScrollPosition(this.scrollPosition, this.scrollOffset);
		}
    }
	
	@Override
	public void finishUpdate(View container) 
	{
		if (transaction != null) 
		{
			 transaction.commit();
			 transaction = null;
			 manager.executePendingTransactions();
        }
    }

	@Override
	public boolean isViewFromObject(View view, Object object) 
	{
		return ((Fragment)object).getView() == view;
	}
	
	private static String makeFragmentName(int viewId, int index) 
	{
        return "android:switcher:" + viewId + ":" + index;
    }

	public Iterator<ScheduleFragment> iterator()
	{
		return activeFragments.values().iterator();
	}
}
