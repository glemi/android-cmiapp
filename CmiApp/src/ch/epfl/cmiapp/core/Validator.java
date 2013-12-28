package ch.epfl.cmiapp.core;

import java.util.List;

class Validator
{
	public enum Relevance { IMPERATIVE, DISJUNCT, OPTIONAL } 
	
	List<Validatable> items;
	
	public interface Validatable
	{
		boolean check();
		Relevance getRelevance();
	}

	void register(Validatable item)
	{
		items.add(item);
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
		public Validatable subject;
		
	}
}

String currentGroup;
for (Setting setting : settings)
{
	if (setting.group != currentGroup)
	validator.register(setting);
}