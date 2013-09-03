package ch.epfl.cmiapp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import android.widget.*;

/*
 * Check this out on how to create section headers!
 * http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 * http://w2davids.wordpress.com/android-listview-with-iconsimages-and-sharks-with-lasers/
 */

public class EqptListAdapter extends CmiPageAdapter
{
	List<CmiEquipment> eqptList;
	LayoutInflater inflater;
	
	public EqptListAdapter(Context context) 
	{
		super();
		Object service = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater = (LayoutInflater) service;
		eqptList = new ArrayList<CmiEquipment>();  
	}

	public int getCount()
	{
		return eqptList.size();
	}
	
	public CmiEquipment getItem(int index)
	{
		return eqptList.get(index);
	}
	
	public long getItemId(int arg0) 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	public View getView (int position, View convertView, ViewGroup parent)
	{
		Log.d("EqptListAdapter", String.format("GetView, Position:%d", position));
		
		View row = convertView;
		if (row == null)
			row = inflater.inflate(R.layout.equipment_listitem, parent, false);

		CmiEquipment eqpt = getItem(position);
		
		TextView nameView = (TextView) row.findViewById(R.id.eqpt_name);
		TextView zoneView = (TextView) row.findViewById(R.id.zone);
		TextView suppView = (TextView) row.findViewById(R.id.supplement);

		nameView.setText(eqpt.name);
		zoneView.setText(String.format("Zone %d", eqpt.zone));
		suppView.setText(eqpt.supplement);
		
		//row.setOnClickListener(this);
		row.setTag(eqpt.machId);
		
		return row;
	}

	public String getEmptyText()
	{
		return new String("No rights for any equipment.");
	}
	
	@Override
	protected void onParseData(Document page)
	{
		// to do: first check if page is what we expected...
		
		Elements elements = page.select("option[Value]");
		
		if (elements.size() > 0)
			eqptList.clear();
		
			
		for(Element element : elements)
		{
			CmiEquipment equipment = new CmiEquipment();

		    Pattern pattern = Pattern.compile("Z(\\d\\d)\\s*([\\w\\s]+)\\s*(?:-\\s([\\w\\s]+))?");
		    Matcher matcher = pattern.matcher(element.text());
		    
		    if (matcher.matches())
		    {
		    	equipment.machId = element.attr("Value");
		    	
		    	equipment.supplement = matcher.group(3);
		    	equipment.name = matcher.group(2);
		    	equipment.zone = Integer.parseInt(matcher.group(1));
		    	eqptList.add(equipment);
		    	
		    	Log.d("CmiParser", equipment.name);
		    }
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		Context context = view.getContext();
		Intent intent = new Intent(context, CmiScheduleActivity.class);
		
		CmiEquipment equipment = eqptList.get(position); 
		
		intent.putExtra("EQUIPMENT_NAME", equipment.name);
    	intent.putExtra("MACHINE_ID", equipment.machId);
    	intent.putExtra("USERNAME", "cnyffeler");
    	intent.putExtra("PASSWORD", "clemens");
    	context.startActivity(intent);
		
	}

}
