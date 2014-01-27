package ch.epfl.cmiapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.core.Configuration.Option;
import ch.epfl.cmiapp.core.Configuration.Setting;
import ch.epfl.cmiapp.core.Configuration;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.Inventory;
import ch.epfl.cmiapp.core.XmlExtractor.ItemNotFoundException;
import ch.epfl.cmiapp.core.XmlLoadedInventory;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class EquipmentManager
{
	
	private static Inventory inventory;
	private AccessibleInventory accessible;
	private Set<String> accessList;
	private Context context;
	
	public static class InventoryLoadException extends Exception
	{
		public InventoryLoadException(Exception cause)
		{
			super("Failed to load Inventory!", cause);
		}
	}
	
	public static Inventory getInventory() { return inventory; }
	
	public static boolean load(Context context)
	{
		EquipmentManager equipmentManager = new EquipmentManager(context);
		return equipmentManager.load();
	}
	
	 
	public static boolean checkConfigParamsCompatible(Equipment test, Equipment reference)
	{
		Configuration testConfig = test.getConfig();  // typically the xml loaded one
		Configuration refConfig  = reference.getConfig(); // typically the web loaded one 
		
		if (testConfig != null && refConfig != null)
		{
			for (Setting testSetting : testConfig)
			{
				String settingId = testSetting.getId();
				Setting refSetting = refConfig.getSetting(settingId);
				if (refSetting == null)
					return false;
				
				for (Option testOption : testSetting)
				{
					String value = testOption.value;
					Option refOption = refSetting.getOption(value);
					if (refOption ==  null)
					{
						Log.d("EquipmentManager.checkConfigParamsCompatible", "Option not found: " + value);
						Log.d("EquipmentManager.checkConfigParamsCompatible", "   Reference Options are ...");
						for (Option refOpt : refSetting)
							Log.d("EquipmentManager.checkConfigParamsCompatible", "   " + refOpt.value + " (" + refOpt.name + ")");	
						return false;
					}
					if (!refOption.name.equals(testOption.name))
					{
						Log.d("EquipmentManager.checkConfigParamsCompatible", "Option found but they seem to be different: ref='" + refOption.name + "' test='" + testOption.name + "'");
						return false;
					}
					Log.d("EquipmentManager.checkConfigParamsCompatible", "Option : " + value + "\t [OK]");
				}
			}
			Log.d("EquipmentManager.checkConfigParamsCompatible", "Everything ok");
			return true;
		}
		else if (testConfig == null && refConfig == null) 
			return true; // both are null; this is ok
		else 
			return false; // one is null the other isn't; not ok.
	}
	
	public EquipmentManager(Context context)
	{
		this.context = context;
		this.accessList = new HashSet<String>();
		this.accessible = new AccessibleInventory();
	}
	
	public Inventory getAccessible()
	{
		return accessible;
	}
	
	public void setAccessible(String machId)
	{
		if (inventory.contains(machId))
		{
			accessList.add(machId);
			accessible.add(machId);
		}
	}
	
	private class AccessibleInventory extends Inventory
	{
		AccessibleInventory()
		{
			
		}
		
		void add(String machId)
		{
			Equipment equipment = EquipmentManager.inventory.get(machId);
			assert(equipment != null);
			super.inventory.put(machId, equipment);
		}
		
		void clear()
		{
			super.inventory.clear();
		}
	}
	
	
	public boolean load()
	{
		if (inventory == null || inventory.isEmpty()) try
		{
			xmlLoad();
			return true;
		}
		catch (InventoryLoadException e)
		{
			e.printStackTrace();
			return false;
		}
		else
			return true;
	}
	
	public void xmlLoad() throws InventoryLoadException
	{
		try 
		{
			Log.d("EquipmentManager.xmlLoad", "loading XML Data.");
			InputStream inStream = context.getResources().openRawResource(R.raw.cmitools);
	
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
	
			Document doc = builder.parse(inStream, null);
			Element rootElement = doc.getDocumentElement();
			rootElement.normalize();
			
			inventory = new XmlLoadedInventory(rootElement);
		}
		catch (Exception e) { throw new InventoryLoadException(e); }
	}
	
	public void saveConfig(String machId)
	{
		Editor data = context.getSharedPreferences("CMI_EQPT_CONFIG", Context.MODE_PRIVATE).edit();
		Equipment equipment = inventory.get(machId);
		
		for (Setting setting : equipment.getConfig())
		{
			String key = machId + ":" + setting.getId(); 
			data.putString(key, setting.getValue());
		}
		data.commit();
	}
	
	public void readConfig(String machId)
	{
		SharedPreferences data = context.getSharedPreferences("CMI_EQPT_CONFIG", Context.MODE_PRIVATE);
		Equipment equipment = inventory.get(machId);
		
		for (Setting setting : equipment.getConfig())
		{
			String key = machId + ":" + setting.getId(); 
			String value = data.getString(key, setting.getValue());
			setting.change(value);
		}
	}
	
	public void saveAccessList()
	{
		Editor data = context.getSharedPreferences("CMI_EQPT_ACCESS", Context.MODE_PRIVATE).edit();
		
		data.putStringSet("accessList", accessList);
		data.commit();
	}
	
	public void clearAccessList()
	{
		accessList.clear();
		accessible.clear();
	}
	
	public void readAccessList()
	{
		SharedPreferences data = context.getSharedPreferences("CMI_EQPT_ACCESS", Context.MODE_PRIVATE);
		
		accessList = data.getStringSet("accessList", new HashSet<String>());
		
		for (String machId : accessList)
			accessible.add(machId);
	}
	
}
