package ch.epfl.cmiapp;

import android.support.v4.app.LoaderManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.content.Loader;

import org.jsoup.nodes.*;

import ch.epfl.cmiapp.CmiLoader.PageType;


/* Progress circle:
 * http://stackoverflow.com/questions/12559461/how-to-show-progress-barcircle-in-an-activity-having-a-listview-before-loading
 * 
 */

public class CmiListActivity extends android.support.v4.app.FragmentActivity 
	implements LoaderManager.LoaderCallbacks<Document>
{
	
	public enum ContentType
	{
		USER_LIST, EQUIPMENT_LIST;
		public static ContentType fromInt(int value) { return ContentType.values()[value];}
	}
	
	TextView emptyView;
	ListView listView;
	CmiPageAdapter adapter;
	ContentType contentType;
	
	
	// http://developer.android.com/design/patterns/actionbar.html
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        
        setContentView(R.layout.activity_cmi_list_activity);
        listView  = (ListView) this.findViewById(android.R.id.list);
        emptyView = (TextView) this.findViewById(android.R.id.empty);
        listView.setEmptyView(emptyView);
        getActionBar().setHomeButtonEnabled(true);
        
        String contentTypeStr;
        
        if (savedInstanceState != null)
        	contentTypeStr = savedInstanceState.getString("CONTENT_TYPE");
        else
        	contentTypeStr =  getIntent().getStringExtra("CONTENT_TYPE");
        
        if (contentTypeStr != null)
        	contentType = ContentType.valueOf(contentTypeStr);
        else
        {  	// get the hell out of here.
        	Intent intent = new Intent(this, MainActivity.class);
        	intent.putExtra("CONTENT_TYPE", "USER_LIST");
        	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	startActivity(intent);
        	return;
        }
        
        PageType pageType;
        switch (contentType)
        {
        case USER_LIST:
        	pageType = PageType.USER_LIST;
        	adapter = new UserListAdapter();
        	setTitle("Users in CMI");
        	break;
        case EQUIPMENT_LIST:
        	pageType = PageType.MAIN_PAGE;
        	adapter = new EqptListAdapter(this);
        	setTitle("My Equipment");
        	break;
    	default:
    		// something is WRONG! argh!
    		return;
        }
        
        if (!CmiEquipment.isEquipmentListLoaded())
        	CmiEquipment.loadEquipmentList(this);
        
        listView.setOnItemClickListener(adapter);
        LoaderManager loaderManager = this.getSupportLoaderManager();
        loaderManager.initLoader(pageType.toInt(), null, this);
    	listView.setAdapter(adapter);
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
		/* this is not ideal yet, we need a type that describes
		 * the kind of activity we want to display here, not the  
		 * kind of cmi page we want to load to that end.
		 */
		setProgressBarVisibility(true);
		setProgressBarIndeterminateVisibility(true);
		setEmptyText("loading...");
		
		CmiLoader cmiLoader = null;
		
		switch (PageType.fromInt(id))
		{
		case MAIN_PAGE:
			cmiLoader = new CmiLoader(this, PageType.MAIN_PAGE);
			break;
		case USER_LIST:
			cmiLoader = new CmiLoader(this, PageType.USER_LIST);
			break;
		}
		return cmiLoader;
	}

	public void onLoadFinished(Loader<Document> loader, Document document) 
	{
		if (document != null)
		{
			adapter.setPage(document);
			setEmptyText(adapter.getEmptyText());	
		}
		else
		{
			setEmptyText("Unable to fetch data from CMI server.");
		}			

		setProgressBarVisibility(false);
		setProgressBarIndeterminateVisibility(false);
	}
	
	private void setEmptyText(String text) 
	{
		TextView textView = (TextView) listView.getEmptyView();
		textView.setText(text);
	}


	public void onLoaderReset(Loader<Document> arg0) 
	{
		
	}
	
	protected void onSaveInstanceState (Bundle outState)
	{
		outState.putString("CONTENT_TYPE", contentType.toString());
	}
	
}
