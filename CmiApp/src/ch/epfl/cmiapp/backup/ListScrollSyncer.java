package ch.epfl.cmiapp;

import java.util.*;

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
	
	public boolean onTouch(View view, MotionEvent event)
	{
		ListView list = (ListView) view;
		
		if (currentTouchSource != null)
		{
			Log.d("ListScrollSyncer.onTouch", "sending touch events to getstureDetector");
			
			list.setOnScrollListener(null);
			return gestureDetector.onTouchEvent(event);
		}
		else
		{
			list.setOnScrollListener(this);
			currentTouchSource = list;

			Log.d("ListScrollSyncer.onTouch", "relaying touch event");
			relayTouchEvent(event);
			
			currentTouchSource = null;
			return false;
		}
	}
	
	private void relayTouchEvent(MotionEvent event)
	{
		for (ListView list : listSet)
		{
			if (list != currentTouchSource)
			{
				list.dispatchTouchEvent(event);
			}
		}
	}
	
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		if (view.getChildCount() > 0)
		{
			currentPosition = view.getFirstVisiblePosition();
			currentOffset   = view.getChildAt(0).getTop();
		}
	}
	
	public void onScrollStateChanged(AbsListView view, int scrollState)	{ }

	// GestureDetector callbacks
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
	public boolean onSingleTapUp(MotionEvent e) { return true; }
	public boolean onDown(MotionEvent e) { return true; }
	public void onLongPress(MotionEvent e) { }
	public void onShowPress(MotionEvent e) { }
	
}
