package ch.epfl.cmiapp.adapters;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.epfl.cmiapp.CmiEquipment;
import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;
import ch.epfl.cmiapp.activities.CmiFragmentActivity;

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
	List<CmiEquipment> eqptList;
	LayoutInflater inflater;
	
	public EqptListAdapter(Context context) 
	{
		super();
		Object service = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater = (LayoutInflater) service;
		eqptList = new ArrayList<CmiEquipment>();  
	}
	
	public CmiEquipment getItem(int position)
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
				String machId = element.attr("Value");
				CmiEquipment equipment = CmiEquipment.getEquipmentByMachId(machId);
				
				if (equipment != null)
					eqptList.add(equipment);
				else
				{
					Log.d("EqptListAdapter.onParseData", "Equipment lookup failed. Using the old-fashioned parsing method.");
					
					equipment = new CmiEquipment();
					if (equipment.parseString(element.text()))
				    {
				    	equipment.machId = machId;
				    	eqptList.add(equipment);
				    	Log.d("EqptListAdapter.onParseData", equipment.name);
				    }
				}
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
		
		CmiEquipment equipment = eqptList.get(innerPosition); 
		
		intent.putExtra("CONTENT_TYPE", "SCHEDULE");
		intent.putExtra("EQUIPMENT_NAME", equipment.name);
    	intent.putExtra("MACHINE_ID", equipment.machId);
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

		CmiEquipment eqpt = eqptList.get(position);
		
		TextView nameView = (TextView) row.findViewById(R.id.eqpt_name);
		TextView zoneView = (TextView) row.findViewById(R.id.zone);
		TextView suppView = (TextView) row.findViewById(R.id.supplement);

		nameView.setText(eqpt.name);
		//zoneView.setText(String.format("Zone %02d", eqpt.zone));
		suppView.setText(eqpt.supplement);
		
		//row.setOnClickListener(this);
		row.setTag(eqpt.machId);
		
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
		String zoneString = String.format("Zone %02d",  eqptList.get(position).zone);
		return zoneString;
	}

}
