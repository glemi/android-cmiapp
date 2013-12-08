package ch.epfl.cmiapp.core;

public class Equipment
{
	protected String machId;
	protected String name;
	protected int zone;
	protected String category;
	protected String supplement;
	protected int slotLength;
	protected String fullString;
	protected Configuration config;
	protected boolean isConfigurable;
	
	public String getMachId() { return machId; }
	public String getName() { return name; } 
	public int getZone() { return zone; } 	
	public String getCategory() { return category; } 	
	public String getSupplement() { return supplement; }	
	public int getSlotLength() { return slotLength; } 	
	public String getFullString() { return fullString; } 
	public Configuration getConfig() { return config; } 	
	public boolean isConfigurable() { return isConfigurable; }

}
