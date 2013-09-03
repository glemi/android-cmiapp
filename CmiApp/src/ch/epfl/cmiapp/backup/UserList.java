package ch.epfl.cmiapp;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

// http://android.cyrilmottier.com/?p=440

// http://w2davids.wordpress.com/android-listview-with-iconsimages-and-sharks-with-lasers/

public class UserList extends Activity 
	implements LoaderManager.LoaderCallbacks<Cursor>
{

	private CmiParser cmi;
	private List<CmiUser> users;
    private UserListAdapter adapter;
    
    public UserList()
    {
    	cmi   = new CmiParser();
		users = null; 
    }

	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        getActionBar().setHomeButtonEnabled(true);
        
        ListView list = (ListView) findViewById(R.id.userListView);
        
        int layout = R.layout.user_listitem;
    	adapter = new UserListAdapter(this, layout, users);    	
    	list.setAdapter(adapter);
   
        DataFetchTask task = new DataFetchTask();
        task.execute();
        
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_user_list, menu);
        return true;
    }

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) 
	{
		// TODO Auto-generated method stub
		
	}

	public void onLoaderReset(Loader<Cursor> arg0) 
	{
		// TODO Auto-generated method stub
		
	}
	
	
	private class DataFetchTask extends AsyncTask<Void, Void, Void> 
	{	
		
		
	    @Override
	    protected Void doInBackground(Void... params)
	    {	      
	    	Log.d("UserList", "doInBackground");
			
	    	try 
			{
				List<CmiUser> users = null;
				users = cmi.getUsers();
				adapter.addAll(users);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
	    	
			return null;
	    }
	    
	    protected void onPostExecute (Void result)
	    {
	    	adapter.notifyDataSetChanged();
	    }
	}
	
}
