package ch.epfl.cmiapp.json;

import java.util.Collections;
import java.util.Iterator;

import org.joda.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.cmiapp.core.CmiSlot;
import ch.epfl.cmiapp.core.Configuration.Values.Value;
import ch.epfl.cmiapp.core.Equipment;

public class JsonableSlot extends CmiSlot
{

	public JsonableSlot(JSONObject source, Equipment equipment) throws JSONException
	{
		super(equipment);
		deserialize(source);		
	}
	
	public JsonableSlot(CmiSlot other)
	{
		super(other);
	}
	
	
	public JSONObject serialize() throws JSONException
	{
		JSONObject main = new JSONObject();
		
		main.put("start", this.start.toString());
		main.put("end", this.end.toString());
		main.put("action", this.action.ordinal());
		main.put("status", this.status.ordinal());
		
		if (this.configValues != null)
		{
			JSONObject config = new JSONObject();
			
			for(Value value : this.configValues)
				config.put(value.setting, value.option);
			
			main.put("config", config);
		}
		
		return main;
	}
	
	
	public void deserialize(JSONObject source) throws JSONException
	{
		String start = source.getString("Start");
		String end = source.getString("end");
		int action = source.getInt("action");
		int status = source.getInt("status");
		
		this.start = LocalDateTime.parse(start);
		this.end   = LocalDateTime.parse(end);
		this.action = BookingAction.values()[action];
		this.status = BookingStatus.values()[status];
		
		JSONObject config = (JSONObject) source.opt("config");
		
		Iterator<String> iter = config.keys();
	    while (iter.hasNext()) 
	    {
	        String setting = iter.next();
	        String option = config.getString(setting);
			this.configValues.set(setting, option);
	    }
	}
	
}














