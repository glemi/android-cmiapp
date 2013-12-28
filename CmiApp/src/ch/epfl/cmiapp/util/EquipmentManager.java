package ch.epfl.cmiapp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.core.Configuration.Setting;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.Inventory;
import ch.epfl.cmiapp.core.XmlLoadedInventory;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class EquipmentManager
{
	private static Inventory inventory;
	private Context context;
	
	public Inventory getInventory()
	{
		return inventory;
	}
	
	public EquipmentManager(Context context)
	{
		this.context = context;
		
		if (inventory.isEmpty()) try
		{
			xmlLoad();
		}
		catch (ParserConfigurationException e)	{ e.printStackTrace(); }
		catch (SAXException e) { e.printStackTrace(); }
		catch (IOException e) {	e.printStackTrace(); }
	}
	
	private void xmlLoad() throws ParserConfigurationException, SAXException, IOException
	{
		InputStream inStream = context.getResources().openRawResource(R.raw.cmitools);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(inStream, null);
		Element rootElement = doc.getDocumentElement();
		rootElement.normalize();
		
		inventory = new XmlLoadedInventory(rootElement);
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
