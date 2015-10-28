package ch.epfl.cmiapp.fragments;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.R.array;
import ch.epfl.cmiapp.R.layout;
import ch.epfl.cmiapp.adapters.UserListAdapter;
import ch.epfl.cmiapp.core.CmiUser;
import ch.epfl.cmiapp.util.CmiLoader;
import ch.epfl.cmiapp.util.ListAnimator;
import ch.epfl.cmiapp.util.CmiLoader.PageType;

import android.app.*;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;

public class UserListFragment extends Fragment
	implements LoaderManager.LoaderCallbacks<Document>
{
	private final int USER_LIST = 0;
	
	private static final int MENU_DIRECTORY_LOOKUP = 0;
	private static final int MENU_CALL_ZONE		   = 1;
	
	UserListAdapter adapter = new UserListAdapter();
	
	private ListAnimator animator;
	
	private ListView listView;
	private TextView emptyView;
	private TextView lastUpdateView;
	
	/* FIXME: Fix inconsistent user-list background behavior
	 * When opening the user list fragment we have 
	 *  - a solid light-grey background
	 *  - white list items 
	 *  
	 * When a long-click is performed on a list item
	 *  - background pattern becomes visible
	 *  - list item is shaded red
	 *  - this reverts back when the click is released 
	 * 
	 * When, after long click, a menu item is pressed  
	 *  (and the user goes back to the list): 
	 *  - the background pattern is now permanently visible
	 *  - the list items show transparency gradient left and right 
	 **/
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view   = inflater.inflate(R.layout.user_list_layout, container, false);
        listView    = (ListView) view.findViewById(android.R.id.list);
        emptyView   = (TextView) view.findViewById(android.R.id.empty);
        lastUpdateView = (TextView) view.findViewById(R.id.lastUpdatedText);
        listView.setAdapter(adapter);
        listView.setEmptyView(emptyView);
        
        animator = new ListAnimator(listView);
        animator.setDuration(600);
		this.registerForContextMenu(listView);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(USER_LIST, null, this);
        
		super.onActivityCreated(savedInstanceState);
	}

	public Loader<Document> onCreateLoader(int id, Bundle args)
	{
		Activity activity = getActivity();
		activity.setProgressBarIndeterminateVisibility(true);
		setEmptyText("loading...");
		
		CmiLoader cmiLoader = new CmiLoader(activity, PageType.USER_LIST);
		cmiLoader.setEnableCachedData(false);
		cmiLoader.setReloadInterval(4);
		return cmiLoader;
	}

	public void onLoadFinished(Loader<Document> id, Document document)
	{
		if (adapter.setPage(document))
		{
			setEmptyText(adapter.getEmptyText());
			String time = org.joda.time.LocalDateTime.now().toString("HH:mm:ss");
			lastUpdateView.setText("last updated " + time);
		}
		else
			setEmptyText("Unable to fetch data from CMI server.");		
		
		this.getActivity().setProgressBarIndeterminateVisibility(false);
	}

	public void onLoaderReset(Loader<Document> id)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void setEmptyText(String text) 
	{
		emptyView.setText(text);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) 
	{

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

	    switch (item.getItemId()) 
	    {
	        case MENU_CALL_ZONE:     
	        	callZone(info.position);
	        	return true;
	        case MENU_DIRECTORY_LOOKUP:	
	        	directoryLookup(info.position);
	        	return true;
	        default:   
	        	return super.onContextItemSelected(item);
	    }
	}

	private void directoryLookup(int position)
	{
		try
		{
			CmiUser user = adapter.getItem(position);
			String fullname = user.fullName().replace(",", "");
			String encoded = URLEncoder.encode(fullname, "utf-8");
			String url = "http://m.epfl.ch/search/directory/?q=" + encoded;
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			startActivity(intent);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	private void callZone(int position)
	{
		CmiUser user = adapter.getItem(position);
				
		String[] numbers = getActivity().getResources().getStringArray(R.array.ZonePhoneNumbers);
		String number;
		
		int index = user.zone - 1;
		
		if(index >= 0 && index < 15)
		{
			number = numbers[index];
		
			Intent callIntent = new Intent(Intent.ACTION_DIAL);          
			callIntent.setData(Uri.parse("tel:" + number));          
			startActivity(callIntent); 
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, view, menuInfo);
		
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		CmiUser user = adapter.getItem(info.position);
		
		menu.setHeaderTitle(user.firstName);
	    menu.add(Menu.NONE, MENU_DIRECTORY_LOOKUP, 0, "Look up in EPFL Directory");
		
		if(user.zone > 0)
			menu.add(Menu.NONE, MENU_CALL_ZONE, 1, "Call Zone " + user.zone);
	}
	
}
