package ch.epfl.cmiapp.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.cmiapp.core.CmiSlot;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.Schedule;
import ch.epfl.cmiapp.core.Configuration.Values.Value;

import android.os.Parcel;

public class JsonableSchedule extends Schedule
{	
	public JsonableSchedule(JSONObject object, Equipment equipment) throws JSONException
	{
		super(equipment);
		deserialize(object);
	}
	
	public JsonableSchedule(Schedule other)
	{
		super(other);
	}

	public JSONObject serialize() throws JSONException
	{
		JSONObject object = new JSONObject();
		CmiSlot[] slots = this.allSlots();		
		JSONArray array = new JSONArray();
	
		for(CmiSlot slot: slots)
		{
			JsonableSlot jslot = new JsonableSlot(slot);
			array.put(jslot.serialize());
		}
		
		object.put("slots", array);
		return object;
	}
	
	public void deserialize(JSONObject json) throws JSONException
	{
		Builder builder = new Schedule.Builder();
		JSONArray array = json.getJSONArray("slots");
		
		
		for(int index = 0; index < array.length(); index++)
		{
			JSONObject jslot = array.getJSONObject(index);
			JsonableSlot slot = new JsonableSlot(jslot, super.getEquipment());
			builder.addSlot(slot);
		}
	}
}
