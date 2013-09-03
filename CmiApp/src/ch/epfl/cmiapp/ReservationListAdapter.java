package ch.epfl.cmiapp;

import java.util.*;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.epfl.cmiapp.CmiEquipment;
import ch.epfl.cmiapp.CmiReservation;
import ch.epfl.cmiapp.CmiSlot;
import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.CmiSlot.BookingStatus;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;
import ch.epfl.cmiapp.adapters.CmiPageAdapter;

import android.content.Context;
import android.util.Log;
import android.view.*;
import android.widget.*;


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
	public int getItemCount()
	{
		return reservations.size();
	}
	
	@Override
	protected void onParseData(Document page)
	{
		if (page == null)
			return;
		
		parseSlots(page);
		
		if (!slots.isEmpty())
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
			{
				reservation = new CmiReservation();
				reservations.add(reservation);
			}

			reservation.insertSlot(next);
			slot = next;
		}
	}
	
	private void parseSlots(Document page)
	{
		Element restable = page.select("table").first();
		Elements rows = restable.select("tr");
		
		if (rows.size() > 0)
		{
			slots.clear();
			reservations.clear();
		}
		
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

			CmiSlot slot = CmiSlot.instantiate(machId, dateTimeString);
			if (slot == null)
				continue;
			
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
	
	public CmiReservation getItem(int position)
	{
		int innerPosition = super.translatePosition(position);
		return reservations.get(innerPosition);
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

		CmiReservation reservation = reservations.get(position);
		
		String machId   = reservation.getMachId();
		String timeSpan = reservation.getStartTime().toString("HH:mm") + " - " + reservation.getEndTime().toString("HH:mm");
		
		CmiEquipment eqpt = CmiEquipment.getEquipmentByMachId(machId);
		
		timeView.setText(timeSpan);
		eqptView.setText(eqpt.name);
		zoneView.setText("Zone " + eqpt.zone);
		
		return row;
	}
	
	@Override
	public int compareSectionsAt(int position1, int position2)
	{
		CmiReservation reservation1 = reservations.get(position1);
		CmiReservation reservation2 = reservations.get(position2);
		
		LocalDate date1 = reservation1.getStartTime().toLocalDate();
		LocalDate date2 = reservation2.getStartTime().toLocalDate();
		
		return Days.daysBetween(date2, date1).getDays();
	}
	
	@Override
	public String getSection(int position)
	{
		CmiReservation reservation = reservations.get(position);
		LocalDate date  = reservation.getStartTime().toLocalDate();
		LocalDate today = new LocalDate();
		
		int dateOffset = Days.daysBetween(today, date).getDays();
		
		switch (dateOffset)
		{
			case 0:  return "Today";
			case 1:  return "Tomorrow";
			default: return date.toString("EEEE, MMM-dd");
		}
	}
}
