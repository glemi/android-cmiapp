package ch.epfl.cmiapp;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.CmiLoader.PageType;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.AbsListView;

public class CmiScheduleActivity extends Activity
	implements LoaderManager.LoaderCallbacks<Document>, ScheduleFragment.ScheduleCallback, 
	AbsListView.OnScrollListener
{

	private static int ID_TODAY = 0;
	private static int ID_TOMORROW = 1;
	
	private String machId;
	
	private SchedulePagerAdapter adapter = null;
	private ViewPager pager = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        
        PagerTitleStrip titleStrip = (PagerTitleStrip) this.findViewById(R.id.pager_title_strip);
        titleStrip.setTextSpacing(120);
        //getActionBar().setHomeButtonEnabled(true);
        
        Bundle arguments = getIntent().getExtras();
        machId = arguments.getString("MACHINE_ID");
        
        this.getActionBar().setHomeButtonEnabled(true);
        
        String eqptName = arguments.getString("EQUIPMENT_NAME");
        setTitle(eqptName);
 
        FragmentManager manager = getFragmentManager();
        adapter = new SchedulePagerAdapter(manager);
        adapter.setArguments(arguments);
        
        
        hideEmptyText();
        pager = (ViewPager) findViewById(R.id.schedulePager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(4);
        
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(0, null, this);
        
    	if (arguments.containsKey("HIGHTLIGHT_SLOTS"))
        {
        	String[] timeStamps = arguments.getStringArray("HIGHTLIGHT_SLOTS");
        	highlightSlots(timeStamps);
        }
        
    }
    
    public void highlightSlots(String[] timeStamps)
    {
    	if(timeStamps.length == 0)
    		return;
    	
    	String timeStamp = timeStamps[0];
    	
    	CmiSlot slot = CmiSlot.instantiate(machId, timeStamp);
    	if (slot == null)
    		return;
    	
    	int dateOffset = slot.getDateOffset();
    	
    	pager.setCurrentItem(dateOffset,  true);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	public Loader<Document> onCreateLoader(int id, Bundle args) 
	{
		int daysToAdd = id; // use a different Loader for different days
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, daysToAdd);
		Date date = calendar.getTime();
		
		CmiLoader cmiLoader = new CmiLoader(this, PageType.MAIN_PAGE_RES);
		cmiLoader.setDateRange(date, date);
		cmiLoader.setMachId(machId);
		
		
		return cmiLoader;
	}

	public void onLoadFinished(Loader<Document> loader, Document document) 
	{
		Log.d("CmiScheduleActivity:onLoadFinished", "start" );
		
		if (document != null)
		{
			adapter.setPage(document);
			hideEmptyText();
		}
		else
		{
			setEmptyText("Unable to fetch data from CMI server.");
		}
	}
	
	public void onLoaderReset(Loader<Document> arg0) 
	{
		// TODO CmiScheduleActivity::onLoaderReset
	}
	
	private void hideEmptyText()
	{
		TextView textView = (TextView) findViewById(R.id.noConnText);
		ViewPager pager = (ViewPager) findViewById(R.id.schedulePager);
		pager.setVisibility(View.VISIBLE);
		textView.setVisibility(View.INVISIBLE);
	}

	private void setEmptyText(String text) 
	{
		TextView textView = (TextView) findViewById(R.id.noConnText);
		ViewPager pager = (ViewPager) findViewById(R.id.schedulePager);
		textView.setVisibility(View.VISIBLE);
		pager.setVisibility(View.INVISIBLE);
		textView.setText(text);
	}

	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{}

	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		int scrollPosition = view.getFirstVisiblePosition();
		int scrollOffset   = view.getChildAt(0).getTop();
		adapter.setScrollPosition(scrollPosition, scrollOffset);
	}

	public void onScheduleChanged()
	{
		this.getLoaderManager().restartLoader(0, null, this);
		
	}

}
