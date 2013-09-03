package ch.epfl.cmiapp;

import java.text.*;
import java.util.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.app.*;


public class ReservationListAdapter extends CmiPageAdapter
{
	
	List<CmiSlot> 				slots = new ArrayList<CmiSlot>();
	List<CmiReservation> reservations = new ArrayList<CmiReservation>();
	
	boolean noReservations = false;

	public boolean isWaitingForData()
	{
		return slots.isEmpty() && !noReservations;
	}
	
	@Override
	protected void onParseData(Document page)
	{
		if (page == null)
			return;
		
		parseSlots(page);
		buildReservations();
	}
	
	private void buildReservations()
	{
		Iterator<CmiSlot> iterator = slots.iterator();
		CmiSlot slot = iterator.next();
		
		CmiReservation reservation = new CmiReservation();
		reservation.insertSlot(slot);
		reservations.add(reservation);
		
		while (iterator.hasNext())
		{
			CmiSlot next = iterator.next();
			if (!slot.isAdjacent(next))
				reservation = new CmiReservation();
			
			reservation.insertSlot(next);
		}
	}
	
	private void parseSlots(Document page)
	{
		Element restable = page.select("table").first();
		Elements rows = restable.select("tr");
		
		if (rows.size() > 0)
			slots.clear();
		
		for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++)
		{
			Element row = rows.get(rowIndex);
			Element tdUser = row.child(0);
			Element tdEqpt = row.child(1);
			Element tdDate = row.child(2);
			Element tdCmt1 = row.child(3);
			Element tdCmt3 = row.child(4);
			
			String userString = tdUser.text();
			String dateTimeString = tdDate.text().substring(0, 18);
			String machId = CmiEquipment.findMachId(tdEqpt.text());
			
			Log.d("ReservationListAdapter.onParseData", tdEqpt.text());

			CmiSlot slot = new CmiSlot(machId, dateTimeString);
			slot.status = CmiSlot.BookingStatus.BOOKED_SELF;
			
			if (!slot.isPast())
				slots.add(slot);
		}
		
		if (slots.isEmpty() && rows.size() > 0)
		{
			// data has been received but no slots have been added
			noReservations = true;
		}
	}
	
	
	@Override
	public String getEmptyText()
	{
		if (noReservations)
			return "No upcoming reserations found.";
		else
			return "Loading...";
	}
	
	@Override
	public View getItemView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;
		Context context = parent.getContext();

		if (row == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.reservations_listitem, parent, false);
		}
		
		TextView timeView = (TextView) row.findViewById(R.id.time);
		TextView eqptView = (TextView) row.findViewById(R.id.equipment);
		TextView zoneView = (TextView) row.findViewById(R.id.zone);

		CmiSlot 	 slot = slots.get(position);
		CmiEquipment eqpt = CmiEquipment.getEquipmentByMachId(slot.getMachId());
		
		timeView.setText(slot.getTimeString());
		eqptView.setText(eqpt.name);
		zoneView.setText("Zone " + eqpt.zone);
		
		return row;
	}
	
	@Override
	public int getItemCount()
	{
		return slots.size();
	}
	
	@Override
	public String getSection(int position)
	{
		CmiSlot slot = slots.get(position);
		return slot.getDateString();
	}
	
}
