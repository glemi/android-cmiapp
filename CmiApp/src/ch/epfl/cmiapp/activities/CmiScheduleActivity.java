package ch.epfl.cmiapp.activities;

import ch.epfl.cmiapp.R;
import android.os.Bundle;
import android.app.FragmentManager;
import android.support.v4.app.FragmentActivity;

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
