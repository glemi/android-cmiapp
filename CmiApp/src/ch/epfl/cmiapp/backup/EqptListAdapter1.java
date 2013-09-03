package ch.epfl.cmiapp;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EqptListAdapter extends BaseAdapter
{

	private CmiParser cmi;
	List<CmiEquipment> eqptList;
	
	
	public EqptListAdapter(Context context) 
	{
		super();
		eqptList = null;
	}
	
	public void addAll(List<CmiEquipment> eqptList)
	{
		if (this.eqptList == null)
			this.eqptList = eqptList;
		else
			eqptList.addAll(eqptList);
	}
	
	public int getCount()
	{
		if (eqptList == null)
			return 0;
		else
		{
			Log.d("EqptListAdapter", String.format("Equipment count:%d", eqptList.size()));
			return eqptList.size();
		}
	}
	
	public CmiEquipment getItem(int index)
	{
		if (eqptList != null)
			return eqptList.get(index);
		else
			return null;
	}
	
	public View getView (int position, View convertView, ViewGroup parent)
	{
		Log.d("EqptListAdapter", String.format("GetView, Position:%d", position));
		
		View row = convertView;
		if (row == null)
		{
			LayoutInflater inflater = (LayoutInflater) 
					this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			row = inflater.inflate(R.layout.equipment_listitem, parent, false);
		}

		CmiEquipment eqpt = getItem(position);
		
		TextView nameView = (TextView) row.findViewById(R.id.eqpt_name);
		TextView zoneView = (TextView) row.findViewById(R.id.zone);
		TextView suppView = (TextView) row.findViewById(R.id.supplement);

		nameView.setText(eqpt.name);
		zoneView.setText(String.format("Zone %d", eqpt.zone));
		suppView.setText(eqpt.supplement);
		
		return row;
	}

	public long getItemId(int arg0) 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	

}
