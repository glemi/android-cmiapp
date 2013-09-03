package ch.epfl.cmiapp.fragments;

import ch.epfl.cmiapp.CompositeListManager;
import ch.epfl.cmiapp.NewsListManager;
import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.ReservationsListManager;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class CompositeListFragment extends Fragment
{
	
	private Bundle arguments = new Bundle();
	private CompositeListManager listManager = new CompositeListManager();
	private ListView list = null;
	
	public ListView getList() { return list; }
	
	@Override
	public void onCreate (Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
			this.arguments = savedInstanceState;
		else
			this.arguments = this.getArguments();
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void setArguments(Bundle args)
	{
		arguments.putAll(args);
		super.setArguments(args);
	}
	
	public void forceDataReload()
	{
		listManager.loadContent(this.getLoaderManager());
		
	}
	
	/*@Override
	public void onAttach (Activity activity)
	{
		super.onAttach(activity);
		listManager.loadContent(this.getLoaderManager());
		
		//for (ListManager manager : managers)
		//	loaderManager.initLoader(manager.getLoaderId(), null, manager);
		
	}*/
	
	@Override
	public void onStart()
	{
		//remember, this comes *after* onCreateView() !
		Log.d("ReservationsFragment.onStart", "on start");
		
		LoaderManager loaderManager = this.getLoaderManager();
		
		listManager.append(new ReservationsListManager(list));
		listManager.append(new NewsListManager(list));
		
		listManager.loadContent(this.getLoaderManager());
		/*for (ListManager manager : managers)
		{
			Loader<Object> loader = loaderManager.getLoader(manager.getLoaderId());
			loader.onContentChanged();
		}*/
		
		//getActivity().setProgressBarIndeterminateVisibility(true);
		super.onStart();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
	    View view = inflater.inflate(R.layout.simple_list_layout, container, false);
	    list = (ListView) view.findViewById(android.R.id.list);
	    listManager.attachList(list);
	    return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		if (arguments != null)
			outState.putAll(arguments);
		super.onSaveInstanceState(outState);
	}
}
