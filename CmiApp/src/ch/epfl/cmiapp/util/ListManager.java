package ch.epfl.cmiapp.util;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.adapters.CmiPageAdapter;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public abstract class ListManager 
	implements LoaderManager.LoaderCallbacks<Document>, OnItemClickListener, 
	OnItemLongClickListener, OnItemSelectedListener, OnCreateContextMenuListener, 
	OnMenuItemClickListener
{
	
	/* Handles Click and other user input events
	 * Provides LoaderCallbacks 
	 * 	creates the required Loader
	 *  relays the loaded result to the adapter
	 * Provides LoaderID of a Loader
	 */
	
	public AbsListView list;
	public abstract int getLoaderId();
	public abstract void onAttachList(AbsListView list);

	public ListManager() {	}
	
	public ListManager(AbsListView list)
	{
		this.list = list;
		onAttachList(list);
	}
	
	public void attachList(AbsListView list)
	{
		this.list = list;
		onAttachList(list);
	}
	
	public abstract BaseAdapter getAdapter();
	public abstract String getTitle();
	
	public Loader<Document> onCreateLoader(int loaderId, Bundle arguments)
	{
		return null; // should be overridden
	}

	public void onLoadFinished(Loader<Document> loader, Document page)
	{
		if (getAdapter() instanceof CmiPageAdapter)
		{
			CmiPageAdapter adapter = (CmiPageAdapter) getAdapter();
			adapter.setPage(page);
		}
	}
	
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		// subclasses should override this method but call through using super.onCreateContextMenu(...)
		
		//AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo; 
		
		//onCreateContextMenu(menu, view, info);
		if (menu != null)
		{
			for (int i = 0; i < menu.size(); i++) if (menu.getItem(i) != null)
				menu.getItem(i).setOnMenuItemClickListener(this);
		}
	}

	public boolean onMenuItemClick(MenuItem item)
	{
		return false;
	}
	
	public void onLoaderReset(Loader<Document> loader) { }
	public void onItemClick(AdapterView<?> list, View view, int position, long itemId) { }
	public boolean onItemLongClick(AdapterView<?> list, View view, int position, long itemId) { return false; }
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)	{ }
	public void onNothingSelected(AdapterView<?> arg0) { }
}
