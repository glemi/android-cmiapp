package ch.epfl.cmiapp.adapters;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import org.jsoup.nodes.*;


public abstract class CmiPageAdapter extends SectionedListAdapter 
	implements AdapterView.OnItemClickListener
{
	
	public boolean setPage(Document page)
	{
		boolean success = false;
		if (page != null)
			success = onParseData(page); // done by subclass
		notifyDataSetChanged();	 // done by superclass
		Log.d("CmiPageAdaper.setPage", "setPage: " + success);
		return success;		
	}
	
	protected abstract boolean onParseData(Document page);

	public abstract String getEmptyText();

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		// default implementation doesn't do anything. 
		
		// this is here so that subclasses don't necessarily have to implement this method. 
		
	}
	
	public void displayLoadingIndicator(boolean enable)
	{
		
	}
	
}
