package ch.epfl.cmiapp.activities;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;
import ch.epfl.cmiapp.R.menu;
import ch.epfl.cmiapp.fragments.CompositeListFragment;
import ch.epfl.cmiapp.fragments.LoginFragment;
import ch.epfl.cmiapp.fragments.NewsFragment;
import ch.epfl.cmiapp.fragments.ReservationsFragment;
import ch.epfl.cmiapp.fragments.LoginFragment.LoginDialogCallbacks;

import android.view.*;
import android.view.View.OnClickListener;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.CalendarContract.Calendars;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class MainActivity extends FragmentActivity 
	implements LoginDialogCallbacks, OnClickListener
{
	
	private LoginFragment dialog = null;
	private ReservationsFragment reservationsFragment = null;
	private NewsFragment newsFragment = null;
	private CompositeListFragment multiFragment = null;
	
	private Button button;

	// check this out for animation
	// http://www.youtube.com/watch?v=mGwG8-chUEM
	
	//http://developer.android.com/design/patterns/actionbar.html
	
	// http://stackoverflow.com/questions/4371273/should-accessing-sharedpreferences-be-done-off-the-ui-thread
	
	/*
	 * Idea for new app design:
	 * Display "my upcoming reservations" by default (done!)
	 * use a DRAWER on the left as main menu. 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * 
	 * Create a new Calendar: "Cmi-Calendar"!
	 * http://developer.android.com/guide/topics/providers/calendar-provider.html#insert-calendar
	 * 
	 * Theme customization:
	 * http://blog.stylingandroid.com/archives/1240
	 * 
	 * Drop Shadow:
	 * http://www.anotherandroidblog.com/2011/06/29/drop-shadow-linearlayout/
	 * 
	 * Calendar events and syncing
	 * http://stackoverflow.com/questions/12458838/create-new-synced-calendar-with-android-api
	 * 
	 * Drawer menu
	 * http://stackoverflow.com/questions/11234375/how-did-google-manage-to-do-this-slide-actionbar-in-android-application
	 * 
	 * Sticky List Headers
	 * https://github.com/emilsjolander/StickyListHeaders
	 * 
	 */
	
	// temporary
	public static final String[] EVENT_PROJECTION = new String[] 
	{
	    Calendars._ID,                           // 0
	    Calendars.ACCOUNT_NAME,                  // 1
	    Calendars.CALENDAR_DISPLAY_NAME,         // 2
	    Calendars.OWNER_ACCOUNT                  // 3
	};
	  
	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
	private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
	// end temporary
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	        .detectDiskReads()
	        .detectDiskWrites()
	        .detectNetwork()   // or .detectAll() for all detectable problems
	        .penaltyLog()
	        .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	        .detectLeakedSqlLiteObjects()
	        .detectLeakedClosableObjects()
	        .penaltyLog()
	        .penaltyDeath()
	        .build());

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);

        //deleteValues();
        SharedPreferences preferences = this.getSharedPreferences("CMI_CREDENTIALS" , MODE_PRIVATE);
        if (!preferences.contains("CMI_USERNAME") &&  dialog == null)
        {
        	LoginFragment dialog = new LoginFragment();
        	dialog.show(this.getFragmentManager(), "login");
        }
        
        this.setContentView(R.layout.activity_main_new);
        
        //this.button = (Button) this.findViewById(R.id.button);
        //this.button.setOnClickListener(this);
        
        //newsFragment 		 = (NewsFragment) getSupportFragmentManager().findFragmentById(R.id.newsFragmentblah);
        //reservationsFragment = (ReservationsFragment) getSupportFragmentManager().findFragmentById(R.id.reservationsFragmentblah);
        multiFragment = (CompositeListFragment) getSupportFragmentManager().findFragmentById(R.id.multiFragment);
        
        //getCalendars();
    }
    
    
    public void getCalendars()
    {
    	// Projection array. Creating indices for this array instead of doing
    	// dynamic lookups improves performance.
    	Log.d("MainActivity.getCalendars", "querying...");
    	
    	// Run query
    	Cursor cur = null;
    	ContentResolver cr = getContentResolver();
    	Uri uri = Calendars.CONTENT_URI;   
    	String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND (" 
    	                        + Calendars.ACCOUNT_TYPE + " = ?) AND ("
    	                        + Calendars.OWNER_ACCOUNT + " = ?))";
    	String[] selectionArgs = new String[] {"sampleuser@gmail.com", "com.google", "sampleuser@gmail.com"}; 
    	// Submit the query and get a Cursor object back. 
    	cur = cr.query(uri, EVENT_PROJECTION, null, null, null);
    	
    	while (cur.moveToNext()) 
    	{
    	    long calID = 0;
    	    String displayName = null;
    	    String accountName = null;
    	    String ownerName = null;
    	      
    	    // Get the field values
    	    calID 		= cur.getLong(PROJECTION_ID_INDEX);
    	    displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
    	    accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
    	    ownerName 	= cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
    	              
    	    Log.d("MainActivity.getCalendars", displayName + "\t (" + accountName + ") \t" + ownerName);
    	}
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	 switch (item.getItemId()) 
    	 {
         case R.id.switchUser:
        	 LoginFragment dialog = new LoginFragment();
         	 dialog.show(this.getFragmentManager(), "login");
             return true;
             
         case R.id.reloadButton:
        	 multiFragment.forceDataReload();
        	 Toast toast = Toast.makeText(this, "reloading...", Toast.LENGTH_LONG);
     		 toast.show();
     		 
         case R.id.vpn_settings:
        	 Intent intent = new Intent("android.net.vpn.SETTINGS");
             intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(intent);
        	 
         default:
             return super.onOptionsItemSelected(item);
    	 }
    }
    
    public void showUserList(View view)
    {
    	Intent intent = new Intent(this, CmiFragmentActivity.class);
    	intent.putExtra("CONTENT_TYPE", "USER_LIST");
    	startActivity(intent);	
    	
    }
    
    public void showEquipmentList(View view)
    {
    	Log.d("MainActivity", "showEquipmentList");
    	Intent intent = new Intent(this, CmiFragmentActivity.class);
    	intent.putExtra("CONTENT_TYPE", "EQUIPMENT_LIST");
    	startActivity(intent);
    }
    

	public void onLoginSuccessful()
	{
		String toastText = "Login Successful.\nWelcome, ";
		toastText += getSharedPreferences("CMI_CREDENTIALS" , MODE_PRIVATE).getString("USER_FULLNAME", "");
		Toast toast = Toast.makeText(this, toastText + "!", Toast.LENGTH_LONG);
		toast.show();
		dialog = null;
		//reservationsFragment.getLoaderManager().restartLoader(0, null, reservationsFragment);
	}

	public void onLoginFailed()
	{
    	dialog.show(this.getFragmentManager(), "login");
	}
	
	private void deleteValues() // for testing purposes
	{
		SharedPreferences preferences = this.getSharedPreferences("CMI_CREDENTIALS", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.remove("CMI_USERNAME");
		editor.remove("CMI_PASSWORD");
		editor.remove("CMI_USERID");
		editor.remove("USER_FULLNAME");
		editor.commit();
	}


	public void onClick(View v)
	{
		((android.graphics.drawable.TransitionDrawable) v.getBackground()).startTransition(1000);
		
	}
	
}
