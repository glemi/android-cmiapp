package ch.epfl.cmiapp;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.cmiapp.CmiEquipment.Configuration.Setting.Required;
import ch.epfl.cmiapp.CmiEquipment.Configuration.Setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class CmiEquipment 
{
	public String machId;
	public String name;
	public int zone;
	public String category;
	public String supplement;
	public int slotLength;
	public String fullString;
	public Configuration config;
	public boolean isConfigurable;
	
	private static XmlEquipmentList staticList = new XmlEquipmentList();
	
	public static class Configuration implements Serializable, Iterable<Setting>
	{
		private static final long	serialVersionUID	= 1L;
		
		public List<Setting> settings = new ArrayList<Setting>();
		public List<Group> groups = new ArrayList<Group>();
		
		private Node root;
		
		public static Configuration fromMachId(String machId)
		{
			CmiEquipment eqpt = getEquipmentByMachId(machId);
			return eqpt.config;
		}
		
		public boolean isCompatible(Configuration other)
		{
			for (Setting setting1 : this.settings)
			for (Setting setting2 : other.settings)
				if (setting1.id == setting2.id && setting1.currentValue != setting2.currentValue)
					return false; 
					
			return true;
		}
		
		public boolean isValid()
		{
			return root.isValid();
		}
		
		public Setting findSetting(String name)
		{
			for (Setting setting : settings) if (setting.name == name)  return setting;
			for (Setting setting : settings) if (setting.title == name) return setting;
			return null;
		}

		public static abstract class Node implements Iterable<Node>
		{
			public final List<Node> children = new ArrayList<Node>();
			
			// imperative: for this setting a valid option (non-0) must be selected
			// disjunct: (c.f. "logical disjunction") out of all settings that have 
			//  the DISJUNCT requirement at least one must be set to a valid option
			public enum Relevance {IMPERATIVE, DISJUNCT}
			
			public String title;
			public String id;
			public Relevance required;
			
			public abstract boolean isValid();
			//public abstract int type();
		}
		
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

			
			public Iterator<Node> iterator()
			{
				return settings.iterator();
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
	
		public static class SettingValidator
		{
		
			void reset()
			{
				
			}
			
			boolean check(Setting setting)
			{
				if(setting.required == Required.DISJUNCT)
				{
					
					
				}
				
				
				return false;
				
			}
			
			boolean getVerdict()
			{
				return false;
			}
			
		}

		public Iterator<Setting> iterator()
		{
			return settings.iterator();
		}
	}
	
	public static void loadEquipmentList(Context context)
	{
		InputStream inStream = context.getResources().openRawResource(R.raw.cmitools);
		staticList.parse(inStream);
	}
	
	public static boolean isEquipmentListLoaded()
	{
		return staticList.isResourceLoaded();
	}
	
	public static CmiEquipment getEquipmentByMachId(String machId) //throws ResourceNotLoadedException
	{
		CmiEquipment eqpt = staticList.findBymachId(machId);
		if (eqpt == null)
		{
			if (machId == null)
				Log.d("CmiEquipment.getEquipmentByMachId", "machId supplied null");
			else
				Log.d("CmiEquipment.getEquipmentByMachId", "not found: " + machId + "(" + machId.length() + ")");
			return null;
		}
		else
			return eqpt;  //new CmiEquipment(eqpt); // clone 
	}
	
	public static CmiEquipment findEquipment(String string) //throws ResourceNotLoadedException
	{
		CmiEquipment eqpt = staticList.findByString(string);
		if (eqpt == null)
			Log.d("CmiEquipment.findEquipment", "not found: " + string);
		return new CmiEquipment(eqpt); // clone 
	}
	
	public static String findMachId(String string)
	{
		CmiEquipment eqpt = staticList.findByString(string);
		if (eqpt == null)
			return "";
		//	throw new RuntimeException("Machine not found: " + string);
		return eqpt.machId;
	}
	
	public CmiEquipment() 
	{
		super();
	}
	
	public CmiEquipment(CmiEquipment clonethis)
	{
		this.machId = clonethis.machId;
		this.name   = clonethis.name;
		this.zone   = clonethis.zone;
		this.category = clonethis.category;
		this.supplement = clonethis.supplement;
		this.slotLength = clonethis.slotLength;
		this.fullString = clonethis.fullString;
	}

	public CmiEquipment(String machId, String name, int zone, String category, String supplement) 
	{
		super();
		this.machId = machId;
		this.name = name;
		this.zone = zone;
		this.category = category;
		this.supplement = supplement;
	}
	
	public boolean parseString(String string)
	{
		//Pattern pattern = Pattern.compile("Z(\\d\\d)\\s*([\\w\\s]+)\\s*(?:-\\s([\\w\\s]+))?");
		Pattern pattern = Pattern.compile("Z(\\d\\d)\\s*([^-]+)(\\s-.*)?");
		Matcher matcher = pattern.matcher(string);
		
		if (matcher.matches())
		{
			if (matcher.group(3) != null)
				this.supplement = matcher.group(3).substring(2);
			
			if (matcher.group(1) != null && matcher.group(2) != null)
			{
				this.name = matcher.group(2);
				this.zone = Integer.parseInt(matcher.group(1));
				return true;
			}
		}
		
		return false;
	}

	public void reloadLastConfig(Context context)
	{
		SharedPreferences data = context.getSharedPreferences("CMI_EQPT_CONFIG", Context.MODE_PRIVATE);
		String content = data.getString(this.machId, null);
		if (content == null) return;
		byte[] bytes = content.getBytes();
				
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		ObjectInput input = null;
		try {
			input = new ObjectInputStream(stream);
			Object object = input.readObject();
			config = (Configuration) object;
		}
		catch (StreamCorruptedException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		catch (ClassNotFoundException e) { e.printStackTrace(); } 
		catch (ClassCastException e) { e.printStackTrace(); }
		finally { try 
			{
				stream.close();
				input.close();
			}
			catch (IOException e) {	e.printStackTrace();}
		}
	} 
	
	public void storeConfig(Context context)
	{
		SharedPreferences data = context.getSharedPreferences("CMI_EQPT_CONFIG", Context.MODE_PRIVATE);
		
		String content = null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ObjectOutput output = null;
		try {
			output = new ObjectOutputStream(stream);   
			output.writeObject(this.config);
			content = stream.toString();
			data.edit().putString(this.machId, content);
		}
		catch (IOException e) { e.printStackTrace(); } 
		finally { try
			{
				output.close();
				stream.close();
			}
			catch (IOException e) { e.printStackTrace(); }
		}
	}
}
