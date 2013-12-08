package ch.epfl.cmiapp;

import java.util.*;

import ch.epfl.cmiapp.core.CmiSlot;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;


// http://stackoverflow.com/questions/12342419/android-scrolling-2-listviews-together

public class ListScrollSyncer
	implements AbsListView.OnScrollListener, OnTouchListener, OnGestureListener
{
	private GestureDetector gestureDetector;
	private Set<ListView>	listSet	= new HashSet<ListView>();
	//private ScrollSyncTask	task	= new ScrollSyncTask();
	//private Handler			handler	= new Handler();
	private ListView currentTouchSource;
	
	private boolean scrolling = false;
	private int currentOffset = 0;
	private int currentPosition = 0;
	
	public void addList(ListView list)
	{
		listSet.add(list);
		list.setOnTouchListener(this);
		list.setSelectionFromTop(currentPosition, currentOffset);
		
		if (gestureDetector == null)
			gestureDetector = new GestureDetector(list.getContext(), this);
	}
	
	public void removeList(ListView list)
	{
		listSet.remove(list);
	}
	
	public void setScrollPosition(int position, int offset)
	{
		currentPosition = position;
		currentOffset   = offset;
		
		for(ListView list : listSet)
		{
			list.setSelectionFromTop(position, offset);
		}
	}
	
	public boolean onTouch(View view, MotionEvent event)
	{
		ListView list = (ListView) view;
		
		if (currentTouchSource != null)
		{
			//Log.d("ListScrollSyncer.onTouch", "sending touch events to getstureDetector");

			list.setOnScrollListener(null);
			boolean value = gestureDetector.onTouchEvent(event);
			
			//Log.d("ListScrollSyncer.onTouch", "RECEIVING SECONDARY TOUCH EVENT " + dateFromList(list) + "\t returning " + value );
			
			return value;
		}
		else
		{
			list.setOnScrollListener(this);
			currentTouchSource = list;

			//Log.d("ListScrollSyncer.onTouch", "relaying touch event coming form " + dateFromList(list));
			//gestureDetector = new GestureDetector(list.getContext(), this);
			gestureDetector.onTouchEvent(event);
			relayTouchEvent(event);
			
			currentTouchSource = null;
			return false;
		}
	}
	
	
	private String dateFromList(ListView list)
	{
		if (list.getChildCount() > 0)
		{
			Object itemtag = list.getChildAt(0).getTag();
			if (itemtag instanceof CmiSlot)
			{
				CmiSlot slot = (CmiSlot) itemtag;
				return slot.getStartTime().toString("EEE.dd");
			}
		}

		return "??";
	}
	
	private void relayTouchEvent(MotionEvent event)
	{
		long startTime = System.nanoTime();    

		
		for (ListView list : listSet)
		{
			if (list != currentTouchSource)
			{
				list.dispatchTouchEvent(event);
			}
		}
		
		long delta = (System.nanoTime() - startTime)/1000000;
		
		Log.d("ListScrollSyncer.relayTouchEvent", "Elapsed time: " + delta);
	}
	
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		if (view.getChildCount() > 0)
		{
			currentPosition = view.getFirstVisiblePosition();
			currentOffset   = view.getChildAt(0).getTop();
		}
	}
	
	public void onScrollStateChanged(AbsListView view, int scrollState)	
	{
		// even if the listviews got out of sync during scrolling, at the end of the scrolling they
		// are forced to the same position.
		if (scrolling && scrollState == SCROLL_STATE_IDLE) // return to IDLE state; end of scrolling
			for(ListView list : listSet) list.setSelectionFromTop(currentPosition, currentOffset);
		scrolling = scrollState != SCROLL_STATE_IDLE;
	}

	// GestureDetector callbacks
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
	{
		//Log.d("ListScrollSyncer.onFling", "RECEIVING SECONDARY TOUCH EVENT onFling");
		return false; 
	}
	
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) 
	{ 
		//Log.d("ListScrollSyncer.onFling", "RECEIVING SECONDARY TOUCH EVENT onScroll");
		return false; 
	}
	
	public boolean onSingleTapUp(MotionEvent e) 
	{ 
		//Log.d("ListScrollSyncer.onSingleTapUp", "RECEIVING SECONDARY TOUCH EVENT onSingleTapUp");
		return true; 
	}
	
	public boolean onDown(MotionEvent e) 
	{ 
		//Log.d("ListScrollSyncer.onDown", "RECEIVING SECONDARY TOUCH EVENT onDown");
		//return true;
		return !scrolling;
	}
	
	public void onLongPress(MotionEvent e) 
	{
		//Log.d("ListScrollSyncer.onLongPress", "RECEIVING SECONDARY TOUCH EVENT onLongPress");
	}
	
	public void onShowPress(MotionEvent e) 
	{
		//Log.d("ListScrollSyncer.onShowPress", "RECEIVING SECONDARY TOUCH EVENT onShowPress");
	}
}
