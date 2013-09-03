package ch.epfl.cmiapp;

import java.io.IOException;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.support.v4.app.NavUtils;

public class EquipmentList extends Activity 
{

	private CmiParser cmi;
	private List<CmiEquipment> equipment;
    private EqptListAdapter adapter;

    
    public EquipmentList()
    {
    	cmi   = new CmiParser();
    	equipment = null; 
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        ListView list = (ListView) findViewById(R.id.equipmentListView);
        
    	adapter = new EqptListAdapter(this);    	
    	list.setAdapter(adapter);
        
    	DataFetchTask task = new DataFetchTask();
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_equipment_list, menu);
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
    
    
    private class DataFetchTask extends AsyncTask<Void, Void, Void> 
	{	
	    @Override
	    protected Void doInBackground(Void... params)
	    {	      
			
	    	try 
			{
				cmi.setCredentials("cnyffeler", "clemens");
				equipment = cmi.getEquipment();
				adapter.addAll(equipment);
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
