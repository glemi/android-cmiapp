package ch.epfl.cmiapp.adapters;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.epfl.cmiapp.CmiUser;
import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;


/*
 * Check this out on how to create section headers!
 * http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 * http://w2davids.wordpress.com/android-listview-with-iconsimages-and-sharks-with-lasers/
 */

public class UserListAdapter extends CmiPageAdapter
{

	private List<CmiUser> users = new ArrayList<CmiUser>();
	private LayoutInflater inflater;
	
	
	public CmiUser getItem(int position)
	{
		int innerPosition = super.translatePosition(position);
		return users.get(innerPosition);
	}
	
	public long getItemId(int position) 
	{
		long superId = super.getItemId(position);
		
		if (superId != 0) return superId; // in this case it's a header.
		else
		{
			int innerPosition = super.translatePosition(position);
			CmiUser user = users.get(innerPosition);
			return user.hashCode(); 
		}
	}
	
	public String getEmptyText()
	{
		return new String("Nobody is currently in the clean room");
	}

	@Override
	protected void onParseData(Document page)
	{
		Elements elements = page.select("td");
		
		if(elements.size() >= 3)
		{
			users.clear();
			
			Iterator<Element> iter = elements.iterator();
			while (iter.hasNext()) 
			{
				CmiUser user = new CmiUser();
				
				//Log.d("GetUsers()", iter.toString());
				
				String nameString = iter.next().text();
				String zoneString = iter.next().text();
				String timeString = iter.next().text();
				
				String company = "";
			    String firstName = "";
			    String lastName = "";
					
				String[] userParts = nameString.split("\\s-\\s");
				
				if (userParts.length == 2)
					company = userParts[1];
				nameString = userParts[0];
				
			    String[] nameParts = nameString.split("\\s");
			    
			    for(String namePart : nameParts)
			    {
			    	if (namePart == namePart.toUpperCase() && namePart.length() > 1)
			    	{
			    		lastName += namePart.charAt(0);
			    		lastName += namePart.substring(1).toLowerCase();
			    		lastName += " ";
			    	}
			    	else
			    	{
			    		firstName += namePart + " ";
			    	}
			    }
			    
			    user.firstName = firstName.trim();
			    user.lastName  = lastName.trim(); 
			    user.company   = company.trim();

			    user.zoneString = zoneString;
			    
				Pattern pattern = Pattern.compile("Zone (\\d+)");
				Matcher matcher = pattern.matcher(zoneString);
				
				if (matcher.matches())
				{
					String zoneNumber = matcher.group(1);
					user.zone = Integer.parseInt(zoneNumber); 
				}
				else if (zoneString.contains("Couloir +1"))
				{
					user.zoneString = "BM +1";
					user.zone = -1;
				}
				else if (zoneString.contains("Couloir -1"))
				{
					user.zoneString = "BM -1";
					user.zone = -2;
				}
				
				user.sinceTime = timeString;
			    
			    users.add(user);
			}
		}		
	}

	@Override
	public View getItemView(int position, View convertView, ViewGroup parent) 
	{
		Context context = parent.getContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View row = convertView;
		if (row == null)
		{
			row = inflater.inflate(R.layout.user_listitem, parent, false);
		}

		CmiUser user = users.get(position);
		
		TextView nameView = (TextView) row.findViewById(R.id.user_name);
		TextView timeView = (TextView) row.findViewById(R.id.since_time);
			
		nameView.setText(user.fullName());
		timeView.setText(String.format("since %s", user.sinceTime));
		
		return row;
	}

	@Override
	public int getItemCount() 
	{
		return users.size();
	}

	@Override
	public String getSection(int position) 
	{
		//Log.d("UserListAdapter.getSection", users.get(position).lastName + " :" + users.get(position).zoneString);
		
		return users.get(position).zoneString;
	}

}

