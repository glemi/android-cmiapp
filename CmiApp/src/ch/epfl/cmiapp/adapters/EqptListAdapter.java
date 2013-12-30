package ch.epfl.cmiapp.adapters;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;
import ch.epfl.cmiapp.activities.CmiFragmentActivity;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.WebLoadedEquipment;
import ch.epfl.cmiapp.util.EquipmentManager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

/*
 * Check this out on how to create section headers!
 * http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 * http://w2davids.wordpress.com/android-listview-with-iconsimages-and-sharks-with-lasers/
 */

public class EqptListAdapter extends CmiPageAdapter
{
	EquipmentManager equipmentManager;
	List<Equipment> eqptList;
	LayoutInflater inflater;
	
	public EqptListAdapter(Context context) 
	{
		super();
		equipmentManager = new EquipmentManager(context);
		Object service = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater = (LayoutInflater) service;
		eqptList = new ArrayList<Equipment>();  
	}
	
	public Equipment getItem(int position)
	{
		int innerPosition = translatePosition(position);
		return eqptList.get(innerPosition);
	}
	
	public long getItemId(int position) 
	{
		// TODO EqptListAdapter::getItemId
		return 0;
	}
	

	public String getEmptyText()
	{
		return new String("No rights for any equipment.");
	}
	
	@Override
	protected boolean onParseData(Document page)
	{
		try 
		{
			// to do: first check if page is what we expected...
			Log.d("EqptListAdapter", "onParseData");
			Elements elements = page.select("option[value^=mach]");
			
			if (elements.size() > 0)
				eqptList.clear();
	
			for(Element element : elements)
			{
				Equipment equipment;
				String machId = element.attr("Value");
				
				
				equipment = equipmentManager.getInventory().get(machId);
				
				if (equipment != null)
				{
					eqptList.add(equipment);
					continue;
				}
				
				Log.d("EqptListAdapter.onParseData", "Equipment lookup failed. Using the old-fashioned parsing method.");
				equipment = WebLoadedEquipment.create(element);
				
				if (equipment != null)
				{
					eqptList.add(equipment);
					continue;
				}
				
				Log.d("EqptListAdapter.onParseData", "Equipment lookup failed. Old-fashioned parsing method failed too!");
			}
			return true;
			
		} catch (RuntimeException exception)
		{
			Log.d("EqptListAdapter.onParseData", "ERROR CAUGHT: MALFORMED CMI PAGE?");
			Log.d("EqptListAdapter.onParseData", exception.getStackTrace().toString());
			return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		Context context = view.getContext();
		Intent intent = new Intent(context, CmiFragmentActivity.class);
		
		int innerPosition = translatePosition(position);
		
		Equipment equipment = eqptList.get(innerPosition); 
		
		intent.putExtra("CONTENT_TYPE", "SCHEDULE");
		intent.putExtra("EQUIPMENT_NAME", equipment.getName());
    	intent.putExtra("MACHINE_ID", equipment.getMachId());
    	context.startActivity(intent);
    	//context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
	}

	@Override
	public View getItemView(int position, View convertView, ViewGroup parent)
	{
		//Log.d("EqptListAdapter", String.format("GetView, Position:%d", position));
		
		View row = convertView;
		if (row == null)
			row = inflater.inflate(R.layout.equipment_listitem, parent, false);

		Equipment eqpt = eqptList.get(position);
		
		TextView nameView = (TextView) row.findViewById(R.id.eqpt_name);
		TextView zoneView = (TextView) row.findViewById(R.id.zone);
		TextView suppView = (TextView) row.findViewById(R.id.supplement);

		nameView.setText(eqpt.getName());
		//zoneView.setText(String.format("Zone %02d", eqpt.zone));
		suppView.setText(eqpt.getSupplement());
		
		//row.setOnClickListener(this);
		row.setTag(eqpt.getMachId());
		
		return row;
	}

	@Override
	public int getItemCount() 
	{
		return eqptList.size();
	}

	@Override
	public String getSection(int position) 
	{
		String zoneString = eqptList.get(position).getZoneString();
		return zoneString;
	}

}
