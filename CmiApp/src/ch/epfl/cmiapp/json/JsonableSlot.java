package ch.epfl.cmiapp.json;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import ch.epfl.cmiapp.core.CmiSlot;
import ch.epfl.cmiapp.core.Configuration.Values.Value;
import ch.epfl.cmiapp.core.Equipment;

public class JsonableSlot extends CmiSlot
{

	public JsonableSlot(JSONObject json, Equipment equipment) throws JSONException
	{
		super(equipment);
		deserialize(json);		
	}
	
	public JsonableSlot(CmiSlot other)
	{
		super(other);
	}
	
	
	public JSONObject serialize() throws JSONException
	{
		JSONObject main = new JSONObject();
		
		main.put("start", this.start.toString("yyyy-MM-dd HH:mm:ss"));
		main.put("action", this.action.ordinal());
		main.put("status", this.status.ordinal());
		main.put("user", this.user);
		main.put("email", this.email);
		main.put("unconfirmed", this.unconfirmed);
		
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
		String start = source.getString("start");
		
		int action = source.getInt("action");
		int status = source.getInt("status");
		
		this.unconfirmed = source.getBoolean("unconfirmed");
		this.setTimeString(start);
		this.action = BookingAction.values()[action];
		this.status = BookingStatus.values()[status];
		this.user = source.getString("user");
		this.email = source.getString("email");
		
		JSONObject config = (JSONObject) source.opt("config");
		
		if (config != null)
		{
			Iterator<String> iter = config.keys();
		    while (iter.hasNext()) 
		    {
		        String setting = iter.next();
		        String option = config.getString(setting);
				this.configValues.set(setting, option);
		    }
		}
	}
	
}














