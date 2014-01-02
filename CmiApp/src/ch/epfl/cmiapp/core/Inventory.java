package ch.epfl.cmiapp.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Inventory implements Iterable<Equipment>
{
	protected Map<String, Equipment> inventory = new HashMap<String, Equipment>();
	
	public boolean isEmpty()
	{
		return inventory.isEmpty();
	}
	
	public Equipment get(String machId)
	{
		return inventory.get(machId);
	}
	
	public Equipment find(String string)
	{	
		for(Equipment eqpt : inventory.values())
		{
			if (string.contains(eqpt.fullString))
				return eqpt;
		}
		return null;
	}

	public Iterator<Equipment> iterator()
	{
		return inventory.values().iterator();
	}	
}
