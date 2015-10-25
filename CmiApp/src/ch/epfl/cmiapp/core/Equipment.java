package ch.epfl.cmiapp.core;

public class Equipment implements Comparable<Equipment>
{
	protected String machId;
	protected String name;
	protected int    zone;
	protected String category;
	protected String supplement;
	protected int    slotLength;
	protected String fullString;
	protected Configuration config;
	protected boolean isConfigurable;
	protected boolean isSupported;
	
	public String  getMachId() { return machId; }
	public String  getName() { return name; } 
	public int     getZone() { return zone; } 	
	public String  getCategory() { return category; } 	
	public String  getSupplement() { return supplement; }	
	public int     getSlotLength() { return slotLength; } 	
	public String  getFullString() { return fullString; } 
	public Configuration getConfig() { return config; } 	
	public boolean isConfigurable() { return isConfigurable; }
	public boolean isSupported() { return isSupported; }

	public String getZoneString()
	{
		return String.format("Zone %02d", zone);
	}
	
	public int compareTo(Equipment other)
	{
		// if they have the same machId they are equal no matter what. 
		if (this.machId.compareTo(other.machId) == 0)
			return 0;
		
		// sort by zone first, then alphabetically
		int zonediff = this.zone - other.zone;
		if (zonediff != 0) 
			return zonediff;
		else
			return this.name.compareTo(other.name);
	}
	
}
