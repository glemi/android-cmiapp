package ch.epfl.cmiapp.fragments;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.util.CompositeListManager;
import ch.epfl.cmiapp.util.ReservationsListManager;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class mainListFragment extends Fragment
{
	private Bundle arguments;
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
		
		listManager.append(new ReservationsListManager());
	}
	
	@Override
	public void setArguments(Bundle args)
	{
		arguments.putAll(args);
		super.setArguments(args);
	}
	
	@Override
	public void onAttach (Activity activity)
	{
		super.onAttach(activity);
		listManager.loadContent(this.getLoaderManager());
		
		//for (ListManager manager : managers)
		//	loaderManager.initLoader(manager.getLoaderId(), null, manager);
		
	}
	
	@Override
	public void onStart()
	{
		Log.d("ReservationsFragment.onStart", "on start");
		
		LoaderManager loaderManager = this.getLoaderManager();
		
		/*for (ListManager manager : managers)
		{
			Loader<Object> loader = loaderManager.getLoader(manager.getLoaderId());
			loader.onContentChanged();
		}*/
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		super.onStart();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
	    View view = inflater.inflate(R.layout.reservations_list, container, false);
	    list = (ListView) view.findViewById(R.id.reservationList);
	    listManager.attachList(list);
	    return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		outState.putAll(arguments);
		super.onSaveInstanceState(outState);
	}
}
