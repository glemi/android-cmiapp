package ch.epfl.cmiapp.util;

import org.jsoup.nodes.Document;

import ch.epfl.cmiapp.adapters.NewsAdapter;
import ch.epfl.cmiapp.adapters.TransientAdapter;
import ch.epfl.cmiapp.adapters.TransientAdapter.TransientMode;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public class NewsListManager extends ListManager
{
	Context context;
	NewsAdapter adapter;
	TransientAdapter wrapper;
	
	public NewsListManager()
	{
		adapter = new NewsAdapter();
		wrapper = new TransientAdapter(adapter);
		
		wrapper.setEmptyMessage("no news.");
		wrapper.setLoadingMessage("loading...");
		wrapper.setFailedMessage("Unable to fetch data from CMi server.");
		wrapper.setMode(TransientAdapter.TransientMode.EMPTY);
	}
	
	public NewsListManager(AbsListView list)
	{
		this();
		super.attachList(list);
	}
	
	@Override
	public int getLoaderId()
	{
		return CmiLoader.PageType.NEWS_PAGE_CONCAT.toInt();
	}
	
	@Override
	public void onAttachList(AbsListView list)
	{
		context = list.getContext();
		//list.setOnCreateContextMenuListener(this);
	}
	
	@Override
	public BaseAdapter getAdapter() { return wrapper; }
	public String getTitle() { return "CMi News"; }
	
	@Override
	public Loader<Document> onCreateLoader(int id, Bundle bundle)
	{
		wrapper.setMode(TransientMode.LOADING);
		
		SharedPreferences preferences = context.getSharedPreferences("CMI_OPTIONS", Context.MODE_PRIVATE);
		int newsItemsCap = preferences.getInt("NEWS_ITEMS_CAP", 5);
		CmiLoader loader = new CmiLoader(context, CmiLoader.PageType.NEWS_PAGE_CONCAT);
		loader.setNewsCap(newsItemsCap);
		return loader;
	}
	
	@Override
	public void onLoadFinished(Loader<Document> loader, Document page)
	{
		if (adapter.setPage(page))
			wrapper.setMode(TransientAdapter.TransientMode.EMPTY);
		else
			wrapper.setMode(TransientAdapter.TransientMode.FAILED);
		
		Log.d("NewsListManager.onLoadFinished", "News Finished Loading.");
	}
	
}
