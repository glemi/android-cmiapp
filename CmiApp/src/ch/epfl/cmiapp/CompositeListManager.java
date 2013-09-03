package ch.epfl.cmiapp;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.LoaderManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.*;
import ch.epfl.cmiapp.adapters.CompositeAdapter;

public class CompositeListManager 
	implements OnItemClickListener, OnItemLongClickListener, OnItemSelectedListener, 
	OnCreateContextMenuListener, OnMenuItemClickListener
{
	
	private CompositeAdapter adapter = new CompositeAdapter();
	private List<ListManager> managers = new ArrayList<ListManager>(); 
	private ListAnimator animator;
	private AbsListView list = null;
	
	public AbsListView 		 getList() { return list; }
	public List<ListManager> getListManagers() { return managers; }

	public CompositeAdapter getAdapter() { return adapter; }
	
	public void append(ListManager manager)
	{
		// only one instance of any type of ListManager is allowed
		for (ListManager man : managers) if (man.getClass().equals(manager.getClass()))
			return;
		
		managers.add(manager);
		adapter.addAdapter(manager.getAdapter(), manager.getTitle());
	}
	
	public void attachList(AbsListView list)
	{
		for (ListManager manager : managers)
			manager.attachList(list);
		
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		//list.setOnItemLongClickListener(this);
		list.setOnItemSelectedListener(this);
		list.setOnCreateContextMenuListener(this);
		//list.setOnHierarchyChangeListener(this);
		
		animator = new ListAnimator(list);
	}
	
	public int[] getLoaderIds()
	{ // not used; replaced by loadContent()
		int n = managers.size();
		int[] ids = new int[managers.size()];
		for (int i = 0; i < n; i++)
			ids[i] = managers.get(i).getLoaderId();
		return ids;
	}
	
	public void loadContent(LoaderManager loaderManager)
	{
		//this comes after attachList;
		//animator.animate();
		
		// instead of initLoader I use restartLoader here which forces a reload 
		for (ListManager listManager : managers)
			loaderManager.restartLoader(listManager.getLoaderId(), null, listManager);
	}
	
	public void onItemSelected(AdapterView<?> list, View view, int position, long itemId)
	{
		int index  = adapter.getAdapterIndexByPosition(position);
		int subpos = adapter.getPartialPosition(position);
		
		ListManager manager = managers.get(index);
		manager.onItemSelected(list, view, subpos, itemId);
	}
	
	public void onNothingSelected(AdapterView<?> list)
	{
		for (ListManager manager : managers)
			manager.onNothingSelected(list);
	}
	
	public boolean onItemLongClick(AdapterView<?> list, View view, int position, long itemId)
	{
		int index  = adapter.getAdapterIndexByPosition(position);
		int subpos = adapter.getPartialPosition(position);
		
		ListManager manager = managers.get(index);
		return manager.onItemLongClick(list, view, subpos, itemId);
	}
	
	public void onItemClick(AdapterView<?> list, View view, int position, long itemId)
	{
		int index  = adapter.getAdapterIndexByPosition(position);
		int subpos = adapter.getPartialPosition(position);
		
		ListManager manager = managers.get(index);
		manager.onItemClick(list, view, subpos, itemId);
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
	    int position = info.position;
	    
	    int index  = adapter.getAdapterIndexByPosition(position);
		int subpos = adapter.getPartialPosition(position);
		
		AdapterContextMenuInfo info1 = new AdapterContextMenuInfo(info.targetView, subpos, info.id);
		
		ListManager manager = managers.get(index);
		manager.onCreateContextMenu(menu, v, info1);
		
		if (menu != null)
			for (int i = 0; i < menu.size(); i++) if (menu.getItem(i) != null)
				menu.getItem(i).setOnMenuItemClickListener(this);
	}
	public boolean onMenuItemClick(MenuItem item)
	{
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		int position = info.position;
		
		int index  = adapter.getAdapterIndexByPosition(position);
		int subpos = adapter.getPartialPosition(position);
		
		ListManager manager = managers.get(index);
				
		info.position = subpos; // this will change the item (no cloning)
		return manager.onMenuItemClick(item);
	}

}
