package ch.epfl.cmiapp.fragments;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.epfl.cmiapp.CmiLoader;
import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.adapters.NewsAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class NewsFragment extends Fragment
	implements LoaderManager.LoaderCallbacks<Document>
{
	private static final int MAIN_PAGE_LOADER = -1;
	private static final int MAX_NEWS_ITEMS = 5;
	
	private NewsAdapter adapter;
	
	private ListView listView    = null;
	private TextView titleView   = null;
	private TextView noItemsView = null;
	private View     loadingView = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
	    View view   = inflater.inflate(R.layout.list_with_header, container, false);
	    
	    Log.d("ReservationsFragment.onCreateView", "on create view");
	    
	    listView    = (ListView) view.findViewById(R.id.list);
	    titleView   = (TextView) view.findViewById(R.id.blubbtitle);
		noItemsView = (TextView) view.findViewById(R.id.emptyNoItems);
		loadingView = 			 view.findViewById(R.id.emptyLoading);
		
		LoaderManager loaderManager = this.getLoaderManager();
        loaderManager.initLoader(MAIN_PAGE_LOADER, null, this);
		adapter = new NewsAdapter();
        
		noItemsView.setText("No News");
		titleView.setText("News");
		listView.setAdapter(adapter);
		listView.setEmptyView(loadingView);
		
		this.registerForContextMenu(listView);
		
	    return view;
	}
	
	private void parseNewsItemLinks(Document document)
	{
		Elements anchors = document.select("a[rel]");
		LoaderManager loaderManager = this.getLoaderManager();
		
		int counter = 0;
		for(Element a : anchors)
		{
			String newsIdString = a.attributes().get("rel");
			int newsId = Integer.parseInt(newsIdString);
			loaderManager.initLoader(newsId, null, this);
			if (++counter > MAX_NEWS_ITEMS) break;
		}
	}
	

	public Loader<Document> onCreateLoader(int loaderId, Bundle arguments)
	{
		CmiLoader loader;
		
		switch(loaderId)
		{
		case MAIN_PAGE_LOADER:
			loader = new CmiLoader(this.getActivity(), CmiLoader.PageType.MAIN_PAGE);
			break;
		default:
			loader = new CmiLoader(this.getActivity(), CmiLoader.PageType.NEWS_PAGE);
			loader.setNewsId(Integer.toString(loaderId));
		}
		return loader;
	}

	public void onLoadFinished(Loader<Document> loader, Document document)
	{
		switch(loader.getId())
		{
		case MAIN_PAGE_LOADER:
			parseNewsItemLinks(document);
			break;
		default:
			adapter.setPage(document);
		}
	}

	public void onLoaderReset(Loader<Document> loader)
	{
		// TODO Auto-generated method stub
		
	}
}
