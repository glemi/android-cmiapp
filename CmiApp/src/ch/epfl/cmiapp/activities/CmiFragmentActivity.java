package ch.epfl.cmiapp.activities;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.R.menu;
import ch.epfl.cmiapp.fragments.EquipmentListFragment;
import ch.epfl.cmiapp.fragments.ScheduleMasterFragment;
import ch.epfl.cmiapp.fragments.UserListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.app.*;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class CmiFragmentActivity extends FragmentActivity
{
	public enum ContentType
	{
		USER_LIST, EQUIPMENT_LIST, SCHEDULE;
		public static ContentType fromInt(int value) { return ContentType.values()[value];}
	}
	
	private ContentType contentType;
	private Bundle arguments; 
	
	private void insertFragment(ContentType contentType)
	{
		FragmentManager manager = this.getSupportFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		
		String tag = contentType.toString();
		Fragment fragment = manager.findFragmentByTag(tag);
        
        if (fragment != null) 
            transaction.attach(fragment);
        else 
        {
        	switch (contentType)
        	{
        	case USER_LIST:
        		fragment = new UserListFragment();
        		this.setTitle("Users in the Cleanroom");
        		break;
        	case EQUIPMENT_LIST:
        		fragment = new EquipmentListFragment();
        		this.setTitle("My Equipment");
        		break;
        	case SCHEDULE:
        		Log.d("FragmentActivity.insertFragment", "creating Schedule master fragment");
        		fragment = new ScheduleMasterFragment();
        		Bundle args = getIntent().getExtras();
        		fragment.setArguments(args);
                String title = args.getString("EQUIPMENT_NAME");
                this.setTitle(title);
        		break;
    		default:
    			throw new RuntimeException("content type not supported by FragmentActivity.");
        	}
        	
        	transaction.add(android.R.id.content, fragment, tag);
        }
        transaction.commit();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);

		String contentTypeStr;
        
        if (savedInstanceState != null)
        {
        	arguments = savedInstanceState;
        	contentTypeStr = savedInstanceState.getString("CONTENT_TYPE");
        }
        else
        {
        	arguments = getIntent().getExtras();
        	contentTypeStr = arguments.getString("CONTENT_TYPE");
        }
        
        if (contentTypeStr != null)
        	contentType = ContentType.valueOf(contentTypeStr);
        else
        	throw new RuntimeException("FragmentActivity called without CONTENT_TYPE parameter.");
        
        insertFragment(contentType);
        updateTitle();
	}
	
	private void updateTitle()
	{
		switch (contentType)
    	{
    	case USER_LIST:   		
    		this.setTitle("Users in the Cleanroom");    
    		break;
    	case EQUIPMENT_LIST:	
    		this.setTitle("My Equipment");	    		
    		break;
    	case SCHEDULE:    		
    		String title = arguments.getString("EQUIPMENT_NAME", "Schedule");
            this.setTitle(title);
    		break;
		default:
			throw new RuntimeException("content type not supported by FragmentActivity.");
    	}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putString("CONTENT_TYPE", contentType.toString());
		outState.putAll(arguments);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fragment_layout, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
