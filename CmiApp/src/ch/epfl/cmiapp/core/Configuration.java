package ch.epfl.cmiapp.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	
	public boolean isValid()
	{
		return root.isValid();
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
	}
	
	public static class Group extends Node implements Iterable<Setting>
	{
		private Group(Node parent) { super(parent); }

		private final List<Setting> settings = new ArrayList<Setting>();

		public Iterator<Setting> iterator()
		{
			return settings.iterator();
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
		protected Group group;
		protected int display;
		
		protected final List<Option> options = new ArrayList<Option>();
		
		public String getName() { return name; }
		public String getValue() { return currentValue; }
		public Group getGroup() { return group; }
		public boolean getsDisplayed() { return display == 0; }
		
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
			if (!this.hasOption(currentValue)) return false;				
			return !currentValue.equals("0");
		}

		public Iterator<Option> iterator()
		{
			return options.iterator();
		}
		
		public List<Option> getOptions()
		{
			List<Option> copy = new ArrayList<Option>();
			for (Option option : options) copy.add(option.clone());
			return copy;
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
