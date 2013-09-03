package ch.epfl.cmiapp;
import java.util.List;

import android.content.Context;
import android.view.*;
import android.widget.*;


//http://w2davids.wordpress.com/android-listview-with-iconsimages-and-sharks-with-lasers/

public class UserListAdapter extends ArrayAdapter<CmiUser> 
{

	private List<CmiUser> users;
	private LayoutInflater inflater;
	
	
	public UserListAdapter(Context context) 
	{
		super(context, R.layout.user_listitem);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addAll(List<CmiUser> users)
	{
		if (this.users == null)
			this.users = users;
		else
			users.addAll(users);
	}
	
	public int getCount()
	{
		if (users == null)
			return 0;
		else
			return users.size();
	}
	
	public CmiUser getItem(int index)
	{
		if (users != null)
			return users.get(index);
		else
			return null;
	}
	
	public View getView (int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		if (row == null)
		{
			row = inflater.inflate(R.layout.user_listitem, parent, false);
		}

		CmiUser user = getItem(position);
		
		TextView nameView = (TextView) row.findViewById(R.id.user_name);
		TextView zoneView = (TextView) row.findViewById(R.id.zone);
		TextView timeView = (TextView) row.findViewById(R.id.since_time);

		nameView.setText(user.firstName + user.lastName);
		zoneView.setText(String.format("Zone %d", user.zone));
		timeView.setText(String.format("since %s", user.sinceTime));
		
		return row;
	}
	
}

