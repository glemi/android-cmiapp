package ch.epfl.cmiapp.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.CmiLoader.PageType;
import ch.epfl.cmiapp.R.layout;

import android.os.Bundle;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.AbsListView;

public class CmiScheduleActivity extends FragmentActivity
{
	private Bundle arguments;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        this.getActionBar().setHomeButtonEnabled(true);
        setTitle("Dummy");
        
        if (savedInstanceState != null)
        	this.arguments = savedInstanceState;
        else
        	this.arguments = this.getIntent().getExtras();
        
        FragmentManager manager = getFragmentManager();

        
    	if (arguments.containsKey("HIGHTLIGHT_SLOTS"))
        {
        	String[] timeStamps = arguments.getStringArray("HIGHTLIGHT_SLOTS");
        	//highlightSlots(timeStamps);
        }
        
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	outState.putAll(arguments);
    }
}
