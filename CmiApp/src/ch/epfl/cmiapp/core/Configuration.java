package ch.epfl.cmiapp.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.cmiapp.core.CmiEquipment.Configuration.Setting;

import android.util.Log;

public class Configuration implements Serializable, Iterable<Setting>
{
	protected List<Setting> settings = new ArrayList<Setting>();
	
	public static class Group extends Node
	{
		public final List<Setting> settings = new ArrayList<Setting>();
		
		@Override
		public boolean isValid()
		{
			if (settings.isEmpty()) 
				return true;
			
			boolean imperativeCondition = true;
			boolean disjunctCondition = false;
			boolean noneDisjunct = true;
			
			for (Setting setting : settings)
			{	
				if (setting.required == Required.DISJUNCT)
				{
					disjunctCondition |= setting.isValid();
					noneDisjunct = false;
				}
				else if (setting.required == Required.IMPERATIVE)
				{
					imperativeCondition &= setting.isValid();
				}
			}
				
			disjunctCondition |= noneDisjunct;
			return imperativeCondition && disjunctCondition;
		}
	}
	
	public static class Setting extends Node
	{
		public String name;   // how cmi web system refers to it
		public String currentValue;
		public String group;
		public int display;
		
		public Type type;
		public enum Type {YESNO, MULTIPLE}
		// imperative: for this setting a valid option (non-0) must be selected
		// disjunct: (c.f. "logical disjunction") out of all settings that have 
		//  the DISJUNCT requirement at least one must be set to a valid option
		public enum Required {IMPERATIVE, DISJUNCT}
		
		public List<Option> options = new ArrayList<Option>();
		
		public Option getCurrent()
		{
			for (Option opt : options) if (opt.value == currentValue) return opt;
			Log.d("CmiEquipment.Setting.getCurrent", "value not found " + currentValue);
			return null;				
		}
		
		public Option findOption(String name)
		{
			for (Option option : options) if (option.name  == name) return option;
			for (Option option : options) if (option.title == name) return option;
			return null;
		}
		
		public boolean isValid()
		{
			// find a better name for this method:
			// something that better reflects that a non-null selection has been made
			// instead of the setting as a whole being "valid" or "invalid"
			return currentValue.equals("0");
		}
		
	}
	
	public static class Option
	{
		public String value; // the id code of this option 219
		public String description; 
		public String title; // what should be displayed to the user
		public String name; // how cmi web system refers to it
		public String toString() { return title; }
	}
}
