package ch.epfl.cmiapp.util;

import java.util.HashMap;

import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.database.DataSetObserver;
import android.graphics.Color;

public class ListAnimator extends DataSetObserver
	implements ViewTreeObserver.OnPreDrawListener, AnimatorListener, OnScrollListener
{
	ListAdapter mAdapter;
    AbsListView mListView;
    
    //BackgroundContainer mBackgroundContainer;
    
    boolean mSwiping = false;
    boolean mItemPressed = false;
    
    boolean isFirstAnimation = true;
    boolean isScrolling = false;
    
    boolean animate = false;
    int ongoingAnimationsCount = 0;
    
    private static final int STD_DURATION = 300;
    int duration = STD_DURATION;
    
    HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();
	private ViewTreeObserver observer;

    
    
    public ListAnimator(AbsListView list)
    {
    	mListView = list;
    	mAdapter  = list.getAdapter();
    	mAdapter.registerDataSetObserver(this);
    	list.setOnScrollListener(this);
    }
    
    public boolean isAnimating()
    {
    	return ongoingAnimationsCount > 0;
    }
    
    public void setDuration(int miliseconds)
    {
    	this.duration = miliseconds;
    }
    
    
    @Override
    public void onChanged()
    {
    	observer  = mListView.getViewTreeObserver();
    	mAdapter  = mListView.getAdapter();
        
    	animate = true;
    	//mListView.setEnabled(false);
        observer.addOnPreDrawListener(this);
    }
    
    
    
    public void mapItems()
    {
        mItemIdTopMap.clear();
    	mAdapter  = mListView.getAdapter();
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        
        for (int i = 0; i < mListView.getChildCount(); ++i) 
        {
            View child   = mListView.getChildAt(i);
            
            
            int position = firstVisiblePosition + i;
            int top		 = child.getTop(); 
            long itemId  = mAdapter.getItemId(position);
            
            mItemIdTopMap.put(itemId, top);
        }
        
        //Log.d("ListAnimator.mapItems", String.format("Mapped %d items", mItemIdTopMap.size()));
    }
    
	public boolean onPreDraw()
	{
		// return false to cancel the drawing - never do that!
		//Log.d("ListAnimator.onPreDraw", String.format("onPreDraw - balance: %d", --balance));
		
		if (animate && !isScrolling)
		{
			// make sure this happens only once!
			if (observer != null &&  observer.isAlive())
				observer.removeOnPreDrawListener(this);
	        observer = null;
	        
	        animate();
	        mapItems();
		}
		else if (!animate && !isScrolling)
		{
			mapItems();
		}
		
		animate = false;
		return true;
	}
	
	private void animate() 
    {	
        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        
        for (int i = 0; i < mListView.getChildCount(); ++i) 
        {
            final View child = mListView.getChildAt(i);
            int position 	 = firstVisiblePosition + i;
            long itemId 	 = mAdapter.getItemId(position);
            Integer startTop = mItemIdTopMap.get(itemId);
            int top 		 = child.getTop();
            
            if (startTop != null) 
            {
            	//Log.d("ListAnimator.onPreDraw", String.format("P%d: found Item %x in map \t previously at %d,\t now at %d", i, itemId, startTop, top));
                if (startTop != top) 
                {              	
                	//Log.d("ListAnimator.onPreDraw", String.format("\t animating Item %x \t from %d \t to %d", itemId, startTop, top));               	
                    int delta = startTop - top;
                    child.setTranslationY(delta);
                    child.animate().setDuration(duration).translationY(0).setListener(this);
                    child.setBackgroundColor(Color.WHITE);
                }
            } 
            else if (!isFirstAnimation)
            {
            	//Log.d("ListAnimator.onPreDraw", String.format("P%d: new   Item %x not in map \t at %d", i, itemId, top));
            	//Log.d("ListAnimator.onPreDraw", String.format("animating Item %x \t (fade in)", itemId));
            	child.setAlpha(0.0f);
            	child.animate().setDuration(duration).setStartDelay(duration).alpha(1.0f).setListener(this);
            }
        }
        
        isFirstAnimation = false;
    }

	public void onAnimationStart(Animator animation) 
	{ 	
		this.ongoingAnimationsCount++; 
		//Log.d("ListAnimator.onAnimationStart", "animation starting " + ongoingAnimationsCount); 
	}
	
	public void onAnimationEnd(Animator animation) 
	{ 
		this.ongoingAnimationsCount--;
		//Log.d("ListAnimator.onAnimationEnd", "animation ending " + ongoingAnimationsCount);
		
		if (ongoingAnimationsCount == 0)
		{
			//Log.d("ListAnimator.onAnimationEnd", "all animations finished");
			//mListView.setEnabled(true);
		}
	}
	
	public void onAnimationCancel(Animator animation) { }
	public void onAnimationRepeat(Animator animation) { }

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		// TODO Auto-generated method stub
		
	}

	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		if (isScrolling && scrollState == SCROLL_STATE_IDLE)
			mapItems();
		isScrolling = scrollState != SCROLL_STATE_IDLE;
	}
        
}
