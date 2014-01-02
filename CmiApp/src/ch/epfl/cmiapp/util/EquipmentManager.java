package ch.epfl.cmiapp.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;


import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.core.Configuration.Setting;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.Inventory;
import ch.epfl.cmiapp.core.XmlExtractor.ItemNotFoundException;
import ch.epfl.cmiapp.core.XmlLoadedInventory;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class EquipmentManager
{
	private static Inventory inventory;
	private Context context;
	
	public static class InventoryLoadException extends Exception
	{
		public InventoryLoadException(Exception cause)
		{
			super("Failed to load Inventory!", cause);
		}
	}
	
	public Inventory getInventory()
	{
		return inventory;
	}
	
	public static boolean load(Context context)
	{
		EquipmentManager equipmentManager = new EquipmentManager(context);
		return equipmentManager.load();
	}
	
	public EquipmentManager(Context context)
	{
		this.context = context;
	}
	
	public boolean load()
	{
		if (inventory.isEmpty()) try
		{
			xmlLoad();
			return true;
		}
		catch (InventoryLoadException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	private void xmlLoad() throws InventoryLoadException
	{
		try 
		{
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
}
