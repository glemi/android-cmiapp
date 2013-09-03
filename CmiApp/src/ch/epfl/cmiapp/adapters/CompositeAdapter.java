package ch.epfl.cmiapp.adapters;

import java.util.ArrayList;
import java.util.List;
import ch.epfl.cmiapp.R;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CompositeAdapter extends BaseAdapter
{
	private class RelayObserver extends DataSetObserver
	{
		@Override
		public void onChanged()
		{
			Log.d("CompositeAdapter.RelayObserver.onChanged", "Data Set Changed (view)");
			CompositeAdapter.this.notifyDataSetChanged();
		}
		
		@Override
		public void onInvalidated()
		{
			Log.d("CompositeAdapter.RelayObserver.onInvalidated", "Data Set Invalidated (view)");
			CompositeAdapter.this.notifyDataSetInvalidated();
		}
	}
	private RelayObserver observer = new RelayObserver();
	
	private List<BaseAdapter> adapters = new ArrayList<BaseAdapter>();
	private List<String> titles = new ArrayList<String>();
	//private ViewTypeMap viewTypeMap = new ViewTypeMap();
	
	private List<Integer> ViewTypeOffsets = new ArrayList<Integer>();

	private static final int PART_HEADER_VIEW_TYPE = 0;
	
	/*private class ViewTypeMap extends HashMap<Integer, HashMap<Integer, Integer>>
	{
		private int viewTypeCounter = 0;
		
		int flatten(int partIndex, int viewType)
		{
			if (this.containsKey(partIndex))
			{
				Map<Integer, Integer> map = this.get(partIndex);
				if (map.containsKey(viewType))
					return map.get(viewType);
				else
				{
					map.put(partIndex, ++viewTypeCounter);
					return viewTypeCounter;
				}
			}
			else
			{
				HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
				map.put(viewType, ++viewTypeCounter);
				this.put(partIndex, map);
				return viewTypeCounter;
			}
		}
	}*/
	
	public void addAdapter(BaseAdapter adapter, String title)
	{
		adapters.add(adapter);
		adapter.registerDataSetObserver(observer);
		titles.add(title);
		notifyDataSetChanged();
	}
	
	public BaseAdapter getAdapter(int index)
	{
		return adapters.get(index);
	}
	
	public BaseAdapter getAdapterByPosition(int position)
	{
		for (int i = 0; i < adapters.size(); i++)
		{
			BaseAdapter adapter = adapters.get(i);
			if (position <= adapter.getCount())
				return adapter;
			else
				position -= adapter.getCount() + 1; // + 1 for header
		}
		return null;
		//throw new RuntimeException("CompositeAdapter.getAdapterByPosition doesn't work.");
	}
	
	public int getAdapterIndexByPosition(int position)
	{
		for (int i = 0; i < adapters.size(); i++)
		{
			BaseAdapter adapter = adapters.get(i);
			if (position <= adapter.getCount())
				return i;
			else
				position -= adapter.getCount() + 1; // + 1 for header
		}
		throw new RuntimeException("CompositeAdapter.getAdapterIndexByPosition doesn't work.");
	}
	
	public int getPartialPosition(int position)
	{
		for (int i = 0; i < adapters.size(); i++)
		{
			BaseAdapter adapter = adapters.get(i);
			position -= 1; // header has position -1
			
			if (position < adapter.getCount())
				return position;
			else
				position -= adapter.getCount();
		}
		throw new RuntimeException("CompositeAdapter.getItem doesn't work.");
	}
	
	public int getCount()
	{
		int count = 0;
		for (BaseAdapter adapter : adapters)
			count += adapter.getCount() + 1; // + 1 because for part headers
		return count;
	}
	
	private View getHeaderView(int partIndex, View convertView, ViewGroup parent)
	{
		Context context 		= parent.getContext();
		Object service  		= context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LayoutInflater inflater = (LayoutInflater) service;	

		View header = convertView;
		//if (header == null)
			header = inflater.inflate(R.layout.partheader, parent, false);

		TextView titleView = (TextView) header.findViewById(R.id.title);
		titleView.setText(titles.get(partIndex));
		
		Log.d("CompositeAdapter.getHeaderView", "\t\t\t\tView Text = " + titles.get(partIndex));
		
		return header;
	}

	public Object getItem(int position)
	{
		for (int i = 0; i < adapters.size(); i++)
		{
			if (position == 0)
				return titles.get(i);
			else 
			{
				position -= 1; // account for title
				BaseAdapter adapter = adapters.get(i);
				if (position < adapter.getCount())
					return adapter.getItem(position);
				else
					position -= adapter.getCount();
			}
		}
		return null;
		//throw new RuntimeException("CompositeAdapter.getItem doesn't work.");
	}

	public long getItemId(int position)
	{
		int partial = getPartialPosition(position);
		int partIndex = getAdapterIndexByPosition(position);
		BaseAdapter adapter = adapters.get(partIndex);
		
		if (partial == -1)
		{
			long nextItemId = adapter.getItemId(0);
			long itemId = 1l << 57 | (partIndex+1l) << 58; // bit# 57 is a 'header' flag
			//Log.d("CompositeAdapter.getItemId", String.format("Id\t @%d \t= %x", position, itemId));
			return itemId;
		}
		else 
		{
			long itemId = adapter.getItemId(partial) | (partIndex+1l) << 58;
			//Log.d("CompositeAdapter.getItemId", String.format("Id\t @%d \t= %x", position, itemId));
			return itemId;
		}
	}
	
	@Override
	public boolean hasStableIds()
	{
		return false;
		/*for (BaseAdapter adapter : adapters)
			if (!adapter.hasStableIds()) return false;
		return true;*/
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		int partial = getPartialPosition(position);
		int partIndex = getAdapterIndexByPosition(position);
		
		int viewtype = this.getItemViewType(position);
		Log.d("CompositeAdapter.getView", "View at pos " + position + "/" + this.getCount() + " : (" + partIndex + " | " + partial + ") \t viewtype = " + viewtype);
		 
		if (partial == -1)
			return getHeaderView(partIndex, convertView, parent);
		else
		{
			BaseAdapter adapter = adapters.get(partIndex);
			return adapter.getView(partial, convertView, parent);
		}
	}
	
	@Override
	public int getViewTypeCount()
	{
		this.ViewTypeOffsets.clear();
		int count = 1; // part headers are one type
		for (BaseAdapter adapter : adapters)
		{
			this.ViewTypeOffsets.add(count);
			count += adapter.getViewTypeCount();
		}
		return count;
	}
	
	@Override
	public int getItemViewType(int position)
	{
		int partial 	= getPartialPosition(position);
		int partIndex 	= getAdapterIndexByPosition(position);
		
		if (partIndex >= this.ViewTypeOffsets.size()) getViewTypeCount();		
		if (partial == -1) return PART_HEADER_VIEW_TYPE;
		
		BaseAdapter adapter = adapters.get(partIndex);
		int viewType  		= adapter.getItemViewType(partial);
		int globalViewType 	= viewType + this.ViewTypeOffsets.get(partIndex);
		
		Log.d("CompositeAdapter.getItemViewType", "Type\t @" + position + /*": (" + partIndex + " | " + partial + ")" */ "\t = "  + viewType + " (local) " + globalViewType + " (global)");
		
		return globalViewType;
	}
	
	@Override
	public boolean isEnabled(int position)
	{
		int partial = getPartialPosition(position);
		int partIndex = getAdapterIndexByPosition(position);
		
		if (partial == -1) return false;

		BaseAdapter adapter = adapters.get(partIndex);
		return adapter.isEnabled(partial);
	}
	
}
