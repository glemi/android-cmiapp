package ch.epfl.cmiapp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.epfl.cmiapp.core.Configuration.Node.Relevance;
import ch.epfl.cmiapp.core.Configuration.Setting;
import android.util.Log;

public class Configuration implements Iterable<Setting>
{
	final Equipment equipment; 
	private List<Setting> settings = new ArrayList<Setting>();
	private Node root;
	
	protected Configuration(Equipment equipment)
	{
		this.equipment = equipment;
		this.root = new Group(null);
		root.id = "0";
		root.title = "Root Configuration Node of " + equipment.machId;
		root.required = Relevance.IMPERATIVE;
	}
	
	protected Configuration(Configuration other)
	{
		this.root = other.root.clone(null);
		this.equipment = other.equipment;
	}
	
	public boolean isValid()
	{
		return root.isValid();
	}
	
	public static class Values implements Iterable<Values.Value> 
	{
		private Map<String, Value> values = new HashMap<String, Value>();
		private Configuration config;
		
		public static class Value
		{
			public Value(String setting, String option)
			{
				this.option = option;
				this.setting = setting;
			}
			public final String option;
			public final String setting; 
		}
		
		public Values(Configuration configuration)
		{
			this.config = configuration;
			for (Setting setting : config) set(setting.id, "0");
		}
		
		public Values(Equipment equipment)
		{
			this.config = equipment.config;
			for (Setting setting : config) set(setting.id, "0");
		}
		
		public void set(String settingId, String optionValue)
		{
			Setting setting = config.findSetting(settingId);
			if (setting == null) return;
			Option option = setting.findOption(optionValue);
			if (option == null) return;	
			Value value = new Value(setting.id, option.value);
			values.put(setting.id, value);
		}
		
		public String get(String idname)
		{
			return values.get(idname).option;
		}

		public Iterator<Value> iterator()
		{
			return values.values().iterator();
		}
		
		@Override
		public String toString()
		{
			String string = "";
			for(Value value : values.values())
			{
				Setting setting = config.getSetting(value.setting);
				Option opt = setting.findOption(value.option);
				if (!opt.value.equals("0"))
					string += "/" + opt.name;
			}
			return string.substring(1);
		}
	}
	
	protected class Builder
	{
		private Group currentGroup = null;
		
		// if not explicitely specified, the generated default constructor is protected, same as the class
		public Builder() { } 

		public Setting createSetting()
		{
			Setting setting;
			if (currentGroup != null)
				setting = new Setting(currentGroup);
			else
				setting = new Setting(root);
			
			settings.add(setting);
			return setting;
		}
		
		public Setting createSetting(Group group)
		{
			Setting setting = new Setting(group);
			settings.add(setting);
			return setting;
		}
		
		public Group startGroup()
		{
			currentGroup = new Group(root);
			return currentGroup;
		}
		
		public void endGroup()
		{
			currentGroup = null;
		}
	}

	public static abstract class Node
	{
		// imperative: for this setting a valid option (non-0) must be selected
		// disjunct: (c.f. "logical disjunction") out of all settings that have 
		//  the DISJUNCT requirement at least one must be set to a valid option
		// optional: this setting is completely optional 
		public enum Relevance {IMPERATIVE, DISJUNCT, OPTIONAL}
		
		protected String title;
		protected String id;
		protected Relevance required;
		
		private final List<Node> children = new ArrayList<Node>();
		
		public String    getTitle()    { return title; }
		public String    getId()       { return id; }
		public Relevance getRequired() { return required; }
		
		private Node(Node parent)
		{
			if (parent != null) parent.children.add(this);
		}
		
		public boolean isValid()
		{
			boolean imperativeCondition = true;
			boolean disjunctCondition = false;
			boolean noneDisjunct = true;
			
			for (Node node : children)
			{	
				if (node.required == Relevance.DISJUNCT)
				{
					disjunctCondition |= node.isValid();
					noneDisjunct = false;
				}
				else if (node.required == Relevance.IMPERATIVE)
				{
					imperativeCondition &= node.isValid();
				}
			}
			disjunctCondition |= noneDisjunct;
			return imperativeCondition && disjunctCondition;
		}
		
		protected abstract Node clone(Node parent);
	}
	
	public static class Group extends Node implements Iterable<Setting>
	{
		private Group(Node parent) { super(parent); }

		private final List<Setting> settings = new ArrayList<Setting>();

		public Iterator<Setting> iterator()
		{
			return settings.iterator();
		}

		@Override
		protected Node clone(Node parent)
		{
			Group cloned = new Group(parent);
			cloned.title = this.title;
			cloned.id 	 = this.id;
			cloned.required = this.required;
			
			for(Setting setting : settings)
				setting.clone(this); // adds itself!
			
			return cloned;
		}
	}
	
	public static class Setting extends Node implements Iterable<Option>
	{
		private Setting(Node parent) { super(parent); }
		private Setting(Group group) 
		{
			super(group); 
			group.settings.add(this);
			this.group = group;
		}

		protected String name;   // how cmi web system refers to it
		protected String currentValue;
		protected Group group = null;
		protected int display;
		
		protected List<Option> options = new ArrayList<Option>();
		
		public String getName() { return name; }
		public String getValue() { return currentValue; }
		public Group getGroup() { return group; }
		public boolean getsDisplayed() { return display != 0; }
		
		public void change(String newValue)
		{
			assert(this.hasOption(newValue));
			currentValue = newValue;
		}

		public void change(Option newOption)
		{
			assert(this.hasOption(newOption.value));
			currentValue = newOption.value;
		}
		
		public boolean hasOption(String optionValue)
		{
			for (Option opt : options) if (opt.value == optionValue) return true;
			return false;
		}
		
		public Option getCurrent()
		{
			for (Option opt : options) if (opt.value.equals(currentValue)) return opt;
			Log.d("CmiEquipment.Setting.getCurrent", "value not found " + currentValue);
			return null;				
		}
		
		public Option findOption(String name)
		{
			for (Option option : options) if (option.value.equals(name)) return option;
			for (Option option : options) if (option.name.equals(name)) return option;
			for (Option option : options) if (option.title.equals(name)) return option;
			return null;
		}
		
		public boolean isValid()
		{
			// find a better name for this method:
			// something that better reflects that a non-null selection has been made
			// instead of the setting as a whole being "valid" or "invalid"
			if (!this.hasOption(currentValue)) return false;				
			return !currentValue.equals("0");
		}

		public Iterator<Option> iterator()
		{
			return options.iterator();
		}
		
		public List<Option> getOptions()
		{
			//List<Option> copy = new ArrayList<Option>();
			//for (Option option : options) copy.add(option.clone());
			/* I would prefer to return a non modifiable list here. Unfortunately this is
			 * not possible. Returning a clone would accomplish more or less the same. 
			 * However sometimes I need the actual references to the real objects. For
			 * example in the ConfigDialog in order for this line in generateViewContent
			 * to work:
			 * adapter.getPosition(setting.getCurrent()); 
			 */
			
			return options;
		}
		
		protected Node clone(Node parent)
		{
			Setting cloned = new Setting(parent);
			cloned.name = this.name;
			cloned.currentValue = this.currentValue;
			cloned.display = this.display;
			cloned.options = this.options; // intentionally a shallow copy!
			return cloned;
		}
		
	}
	
	public static class Option
	{
		public String name; // how cmi web system refers to it
		public String title; // what should be displayed to the user
		public String value; // the id code of this option 219
		public String description; 
		
		public String toString() { return title; }
		protected Option clone()
		{
			Option option = new Option();
			option.name = name;
			option.title = title;
			option.value = value;
			option.description = description;
			return option;
		}
	}
	
	public Iterator<Setting> iterator()
	{
		return settings.iterator();
	}
	
	public Setting getSetting(int index)
	{
		return settings.get(index);
	}
	
	public Setting getSetting(String id)
	{
		for (Setting setting : settings)
			if (setting.id.equals(id)) return setting;
		return null;
	}
	
	public Setting findSetting(String name)
	{
		for (Setting setting : settings)
			if (setting.id.equals(name)) return setting;
		
		for (Setting setting : settings)
			if (setting.name.equals(name)) return setting;
		return null;
	}
	
	public int getSettingsCount()
	{
		return settings.size();
	}
}




/*//http://en.wikipedia.org/wiki/Logical_connective


class Validator
{
	public interface Validatable
	{
		boolean check();
		Relevance getRelevance();
	}

	void register(Validatable)
	{
	
	}
	
	void group(Relevance)
	{
		
	}
	
	boolean check()
	{
	
		for (Item item : list)
		{
			if (Item)	
			
		}
	}
	
	
	private static class Item
	{
		
	}
}

String currentGroup;
for (Setting setting : settings)
{
	if (setting.group != currentGroup)
	validator.register(setting);
}
*/
