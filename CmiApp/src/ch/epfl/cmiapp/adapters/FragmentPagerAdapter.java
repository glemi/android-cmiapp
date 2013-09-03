package ch.epfl.cmiapp.adapters;

import java.util.HashMap;
import java.util.Iterator;

import android.support.v4.app.*;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public abstract class FragmentPagerAdapter extends PagerAdapter
	implements Iterable<Fragment>
{
	
	private final FragmentManager manager;
    private FragmentTransaction transaction = null;
    protected HashMap<Integer, Fragment> activeFragments = new HashMap<Integer, Fragment>();
	
	
    public FragmentPagerAdapter(FragmentManager manager)
    {
    	this.manager = manager;
    }
    
    public abstract Fragment createFragment(int position);
    
    protected String getTag(int viewId, int index)
	{
		return "android:switcher:" + viewId + ":" + index;
	}    
    
	@Override
	public int getCount()
	{
		// this will be overridden
		return 0;
	}
	
	public Fragment getFragment(int position)
	{
		// usually there are 3 fragments active: the currently displayed one
		// plus one on the left and right. This is determined by 
		// ViewPager::setOffscreenPageLimit(int limit)
		return activeFragments.get(position);
	}
	
	public Fragment getFragment(String tag)
	{
		return manager.findFragmentByTag(tag);
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return ((Fragment)object).getView() == view;
	}

	public Iterator<Fragment> iterator()
	{
		return activeFragments.values().iterator();
	}

	@Override
	public final void destroyItem(ViewGroup container, int position, Object object)
	{
		 if (transaction == null) 
			 transaction = manager.beginTransaction();
	        
		 transaction.detach((Fragment)object);
		 activeFragments.remove(position);
	}

	@Override
	public final Object instantiateItem(ViewGroup container, int position)
	{
		if (transaction == null) 
            transaction = manager.beginTransaction();

        Log.d("SchedulePagerAdapter.instantiateItem", "container ID = " + container.getId());
        // Do we already have this fragment?
        String tag = getTag(container.getId(), position);
        Fragment fragment = manager.findFragmentByTag(tag);
        
        if (fragment != null) 
            transaction.attach(fragment);
        else 
        {
    		fragment = createFragment(position);
    		transaction.add(container.getId(), fragment, tag);
        }

        activeFragments.put(position, fragment);
        
        return fragment;
	}
	
	@Override
	public void finishUpdate(View container) 
	{
		if (transaction != null) 
		{
			 //http://stackoverflow.com/questions/12276243/commit-fragment-from-onloadfinished-within-activity
			 transaction.commitAllowingStateLoss();
			 transaction = null;
			 manager.executePendingTransactions();
        }
    }
	
}
