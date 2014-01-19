package ch.epfl.cmiapp.fragments;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.R.anim;
import ch.epfl.cmiapp.R.layout;
import ch.epfl.cmiapp.activities.CmiFragmentActivity;
import ch.epfl.cmiapp.adapters.EqptListAdapter;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.util.CmiLoader;
import ch.epfl.cmiapp.util.CmiLoader.PageType;
import ch.epfl.cmiapp.util.EquipmentManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EquipmentListFragment extends Fragment
	implements LoaderManager.LoaderCallbacks<Document>, AdapterView.OnItemClickListener
{
	private ListView listView;
	private TextView emptyView;
	private EqptListAdapter adapter;
	private EquipmentManager manager;
	
	private enum Status {LOADING, FAILED_LOADING, NORMAL}
	
	@Override
	public void onAttach(Activity activity)
	{
		// important: instantiate EquipmentManager before the adapter!
		manager = new EquipmentManager(activity);
		manager.load(); 
		adapter = new EqptListAdapter(activity);
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.activity_cmi_list_activity, container, false);
		
		listView  = (ListView) view.findViewById(android.R.id.list);
        emptyView = (TextView) view.findViewById(android.R.id.empty);
        listView.setEmptyView(emptyView);
		
		
        listView.setOnItemClickListener(this);
        LoaderManager loaderManager = this.getLoaderManager();
        loaderManager.initLoader(0, null, this);
    	listView.setAdapter(adapter);
        
		return view;
	}
	
	
	public Loader<Document> onCreateLoader(int id, Bundle args) 
	{
		changeStatus(Status.LOADING);
		return new CmiLoader(this.getActivity(), PageType.MAIN_PAGE);
	}

	public void onLoadFinished(Loader<Document> loader, Document document)
	{
		if (adapter.setPage(document))
			changeStatus(Status.NORMAL);	
		else
		{
			changeStatus(Status.FAILED_LOADING);
			
			String text = "Loading failed: list may not be up to date.";
			Toast toast = Toast.makeText(this.getActivity(), text, Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public void onLoaderReset(Loader<Document> loader)
	{
		
	}
	
	public void changeStatus(Status status)
	{
		switch (status)
		{
		case NORMAL:
			getActivity().setProgressBarIndeterminateVisibility(false);
			emptyView.setText("loading...");
			break;
		case FAILED_LOADING:
			getActivity().setProgressBarIndeterminateVisibility(false);
			emptyView.setText("Unable to fetch data from CMI server.");
			break;
		case LOADING:
			getActivity().setProgressBarIndeterminateVisibility(true);
			emptyView.setText("loading...");
			break;
		}
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Context context = view.getContext();
		Activity activity = this.getActivity();
		
		Intent intent = new Intent(activity, CmiFragmentActivity.class);

		Equipment equipment = adapter.getItem(position);
		
		intent.putExtra("CONTENT_TYPE", "SCHEDULE");
		intent.putExtra("EQUIPMENT_NAME", equipment.getName());
    	intent.putExtra("MACHINE_ID", equipment.getMachId());
    	
    	activity.startActivity(intent);
    	activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
		
	}
	
}
