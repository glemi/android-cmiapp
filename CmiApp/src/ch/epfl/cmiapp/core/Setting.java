package ch.epfl.cmiapp.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;
import ch.epfl.cmiapp.core.Configuration.Option;

public class Setting implements Iterable<Option>
{
	protected String name;   // how cmi web system refers to it
	protected String currentValue;
	protected String group;
	protected int display;
	
	protected final List<Option> options = new ArrayList<Option>();
	
	public String getName() { return name; }
	
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
}
