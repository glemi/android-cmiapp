package ch.epfl.cmiapp.adapters;

import ch.epfl.cmiapp.R;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TransientAdapter extends BaseAdapter implements OnClickListener
{
	public interface ReloadRequestReceiver
	{
		public void requestReload();
	}
	
	private class RelayObserver extends DataSetObserver
	{
		@Override
		public void onChanged()
		{
			TransientAdapter.this.notifyDataSetChanged();
		}
		 
		@Override
		public void onInvalidated()
		{
			TransientAdapter.this.notifyDataSetChanged();
		}
	}
	
	private BaseAdapter adapter;
	private TransientMode mode;
	
	private String loadingMessage;
	private String emptyMessage;
	private String failedMessage;
	
	private boolean displayPlaceholder = true;
	private boolean enableTapToRetry = false;
	
	private ReloadRequestReceiver receiver = null;
	
	public static final int EXTRA_VIEW_TYPES = 3; 
	public static final int EMPTY_TYPE = 0;
	public static final int LOADING_TYPE = 1;
	public static final int FAILED_TYPE = 2;
	
	public enum TransientMode { SIMPLE, LOADING, EMPTY, FAILED }
	
	
	
	public TransientAdapter(BaseAdapter adapter)
	{
		this.adapter = adapter;
		adapter.registerDataSetObserver(new RelayObserver());
	}
	
	public void setMode(TransientMode mode)
	{
		this.mode = mode;
		notifyDataSetChanged();
	}
	
	public void enableTapToRetry(ReloadRequestReceiver reloadRequestReceiver)
	{
		this.receiver = reloadRequestReceiver;
	}
	
	public void disableTapToRetry()
	{
		this.receiver = null;
	}
	
	public void setLoadingMessage(String message)
	{
		loadingMessage = message;
	}
	
	public void setEmptyMessage(String message)
	{
		emptyMessage = message;
	}
	
	public void setFailedMessage(String message)
	{
		failedMessage = message;
	}
	
	public int getCount()
	{
		//Log.d("TransientAdapter.getCount", "inner adapter count: " + adapter.getCount());
		
		if (adapter.getCount() == 0 && mode != TransientMode.SIMPLE)
			return 1;
		else
			return adapter.getCount();
	}
	
	public Object getItem(int position)
	{
		if (adapter.getCount() == 0)
			return null;
		else
			return adapter.getItem(position);
	}
	
	public long getItemId(int position)
	{
		if (adapter.getCount() == 0)
			return 0;
		else
			return adapter.getItemId(position);
	}
	
	@Override
	public boolean hasStableIds()
	{
		return adapter.hasStableIds();
	}
	
	@Override
	public int getItemViewType(int position)
	{
		if (adapter.getCount() == 0)
			switch (mode)
			{
				case EMPTY: 	return getViewTypeCount() - EMPTY_TYPE;
				case LOADING: 	return getViewTypeCount() - LOADING_TYPE;
				case FAILED:	return getViewTypeCount() - FAILED_TYPE;
				default: throw new RuntimeException();
			}
		else
			return adapter.getItemViewType(position);
	}
	
	@Override
	public int getViewTypeCount()
	{
		return adapter.getViewTypeCount() + EXTRA_VIEW_TYPES;
	}
	
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (adapter.getCount() > 0)
			return adapter.getView(position, convertView, parent);
		
		Context context = parent.getContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View item = convertView;
		TextView textView = null;
		ImageView retryIcon = null;
		
		switch (mode)
		{
			case EMPTY:
				//if (item == null) 
					item = inflater.inflate(R.layout.empty_listitem, parent, false); 
				textView = (TextView) item.findViewById(R.id.emptyText);
				textView.setText(emptyMessage);
				Log.d("TransientAdapter.getView", "\t\t\t\tView Text = " + emptyMessage);
				break;
			case LOADING: 	
				//if (item == null) 
					item = inflater.inflate(R.layout.loading_listitem, parent, false);
				textView = (TextView) item.findViewById(R.id.loadingText);
				textView.setText(loadingMessage);
				Log.d("TransientAdapter.getView", "\t\t\t\tView Text = " + loadingMessage);
				break;
			case FAILED:
				//if (item == null) 
					item = inflater.inflate(R.layout.failed_listitem, parent, false);
				textView = (TextView) item.findViewById(R.id.failedText);
				retryIcon = (ImageView) item.findViewById(R.id.retry);
				textView.setText(failedMessage + (enableTapToRetry ? "\n Tap to Retry" : ""));
				retryIcon.setVisibility(enableTapToRetry ? View.VISIBLE : View.INVISIBLE);
				Log.d("TransientAdapter.getView", "\t\t\t\tView Text = " + failedMessage);
				item.setOnClickListener(this);
				break;
		}
		return item;
	}
	
	@Override
	public boolean isEnabled(int position)
	{
		if (adapter.getCount() == 0)
			return false;
		else
			return adapter.isEnabled(position);
	}

	public void onClick(View v)
	{
		if (this.receiver != null)
			this.receiver.requestReload();
	}
	
}
