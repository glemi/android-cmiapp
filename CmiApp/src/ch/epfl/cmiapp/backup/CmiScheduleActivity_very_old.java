package ch.epfl.cmiapp;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.CmiLoader.PageType;

import android.os.Bundle;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;

public class CmiScheduleActivity extends FragmentActivity
	implements LoaderManager.LoaderCallbacks<Document>
{

	private static int ID_TODAY = 0;
	private static int ID_TOMORROW = 1;
	
	private String machId;
	
	private List<ScheduleAdapter> adapters = null;
	
	private SchedulePagerAdapter adapter = null;
	private ViewPager pager = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        FragmentManager manager = this.getSupportFragmentManager();
        adapter = new SchedulePagerAdapter(manager);
        
        pager = (ViewPager) findViewById(R.id.schedulePager);
        pager.setAdapter(adapter);
        
        machId = getIntent().getStringExtra("MACHINE_ID");
        
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(ID_TODAY, null, this);
        loaderManager.initLoader(ID_TOMORROW, null, this);
        loaderManager.initLoader(ID_TODAY + 2, null, this);
        loaderManager.initLoader(ID_TODAY + 3, null, this);
        loaderManager.initLoader(ID_TODAY + 4, null, this);
        loaderManager.initLoader(ID_TODAY + 5, null, this);
    	
        
        adapters.add(new ScheduleAdapter(this));
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
		
		CmiLoader cmiLoader = new CmiLoader(this, PageType.RESERVATIONS_PAGE);
		cmiLoader.setDateRange(date, date);
		cmiLoader.setMachId(machId);
		
		return cmiLoader;
	}

	public void onLoadFinished(Loader<Document> loader, Document document) 
	{
		// TODO Auto-generated method stub
		
	}

	public void onLoaderReset(Loader<Document> arg0) 
	{
		// TODO Auto-generated method stub
		
	}

}
