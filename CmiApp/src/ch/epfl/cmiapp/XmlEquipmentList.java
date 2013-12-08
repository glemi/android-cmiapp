package ch.epfl.cmiapp;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ch.epfl.cmiapp.CmiEquipment.Configuration;
import ch.epfl.cmiapp.CmiEquipment.Configuration.*;
import ch.epfl.cmiapp.CmiEquipment.Configuration.Node.*;
import ch.epfl.cmiapp.CmiEquipment.Configuration.Setting.*;

import android.util.Log;

public class XmlEquipmentList 
{	
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private List<CmiEquipment> list = null;
	
	
	public class ResourceNotLoadedException extends RuntimeException
	{
		ResourceNotLoadedException()
		{
			super("The Equipment List has not been loaded from XML Resource.");
		}
	}
	
	public boolean isResourceLoaded()
	{
		return this.list != null;
	}
	
	public CmiEquipment findBymachId(String machId)// throws ResourceNotLoadedException
	{
		if (list == null)
			return null;
			//throw new ResourceNotLoadedException();
			
			
		for(CmiEquipment eqpt : list)
		{
			if (eqpt.machId.equals(machId))
				return eqpt;
		}
		return null;
	}
	
	public CmiEquipment findByString(String fullString) // throws ResourceNotLoadedException
	{
		if (list == null)
			throw new ResourceNotLoadedException();
		
		for(CmiEquipment eqpt : list)
		{
			if (fullString.contains(eqpt.fullString))
				return eqpt;
		}
		
		Log.d("CmiEquipment.findByString", fullString + " not found; Size of EqptList = " + list.size());
		// try fuzzy search?
		return null;
	}
	
	
	public List<CmiEquipment> getList() 
	{
		return this.list;
	}
	
	public void parse(InputStream inStream) 
	{
		this.list = new ArrayList<CmiEquipment>();
		
		try {
			this.factory = DocumentBuilderFactory.newInstance();
			this.builder = this.factory.newDocumentBuilder();
			this.builder.isValidating();
			
			Document doc = this.builder.parse(inStream, null);
			doc.getDocumentElement().normalize();
	
			NodeList eqptNodeList = doc.getElementsByTagName("cmitool");
			final int length = eqptNodeList.getLength();
	
			for (int i = 0; i < length; i++) 
			{
				final Node toolNode =  eqptNodeList.item(i);
				final NamedNodeMap attr = toolNode.getAttributes();
				
				final String machId 		= getNodeValue(attr, "machId");
				final String zoneStr 		= getNodeValue(attr, "zone");
				final String slotLengthStr 	= getNodeValue(attr, "slotLength");
				final String name   		= getNodeValue(attr, "name");
				final String description 	= getNodeValue(attr, "description");
				final String fullString 	= getNodeValue(attr, "fullString");
				
				final int zone = Integer.parseInt(zoneStr);
				final int slotLength = Integer.parseInt(slotLengthStr);
				
				CmiEquipment equipment = new CmiEquipment();
				
				equipment.machId = machId;
				equipment.name   = name;
				equipment.zone   = zone;
				equipment.supplement = description;
				equipment.slotLength = slotLength;
				equipment.fullString = fullString;
				equipment.isConfigurable = false;
				
				if (toolNode.hasChildNodes())
				{
					Node configNode = toolNode.getChildNodes().item(1);
					equipment.config = parseConfigNode(configNode);
					equipment.isConfigurable = true;
				}
				else
					equipment.config = new CmiEquipment.Configuration();
				
				// Add to list
				this.list.add(equipment);
				
				Log.d("EquipmentXMLParser.parse", equipment.name);
			}
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	private CmiEquipment.Configuration parseConfigNode(final Node configNode)
	{
		CmiEquipment.Configuration config = new CmiEquipment.Configuration();
		NodeList settingNodes = configNode.getChildNodes();
		
		for(int index = 0; index < settingNodes.getLength(); index++)
		{
			Node node = settingNodes.item(index);
			String nodeName = node.getNodeName();
			
			if (nodeName.equals("setting"))
			{
				Setting setting = parseSettingNode(node);
				config.settings.add(setting);
			}
			else if (nodeName.equals("group"))
			{
				Group group = parseGroupNode(node);
				config.groups
				config.settings.addAll(group.settings);
			}
		}
		return config.settings.size() > 0 ? config : null;
	}
	
	private CmiEquipment.Configuration.Setting parseSettingNode(final Node settingNode)
	{
		CmiEquipment.Configuration.Setting setting = new CmiEquipment.Configuration.Setting();
		setting.currentValue 	= getStringAttr(settingNode, "default", "0");
		setting.id 				= getStringAttr(settingNode, "id");
		setting.title 			= getStringAttr(settingNode, "title");
		setting.name 			= getStringAttr(settingNode, "name");
		setting.display			= getIntAttr(settingNode, "display", 1);
		
		String required   		= getStringAttr(settingNode, "required");
		setting.required 		= Required.valueOf(required);
		setting.type  			= Type.MULTIPLE;
		
		NodeList optionNodes = settingNode.getChildNodes();
		
		for(int index = 0; index < optionNodes.getLength(); index++)
		{
			Node optionNode = optionNodes.item(index);
			if (optionNode.getNodeName().equals("option"))	
			{
				Option option = new Option();
				option.value = 		getStringAttr(optionNode, "value");
				option.title =  	getStringAttr(optionNode, "title");
				option.name  =  	getStringAttr(optionNode, "name");
				option.description = option.title;
				setting.options.add(option);
			}
		}

		return setting;
	}
	
	private CmiEquipment.Configuration.Group parseGroupNode(final Node groupNode)
	{
		CmiEquipment.Configuration.Group group = new Group();
		
		group.id 		= getStringAttr(groupNode, "id");
		group.title 	= getStringAttr(groupNode, "title");
		String required = getStringAttr(groupNode, "required");
		group.required 	= Configuration.Node.Relevance.valueOf(required);
		
		NodeList settingNodes = groupNode.getChildNodes();
		
		for(int index = 0; index < settingNodes.getLength(); index++)
		{
			Node settingNode = settingNodes.item(index);
			String nodeName = settingNode.getNodeName();
			
			if (nodeName.equals("setting"))
			{
				Setting setting = parseSettingNode(settingNode);
				setting.group = group.id;
				group.settings.add(setting);
			}
		}
		return group;
	}
	

	private int getIntAttr(Node node, String attrName)
	{
		String string = getStringAttr(node, attrName);
		return Integer.parseInt(string);
	}
	
	private int getIntAttr(Node node, String attrName, int defaultValue)
	{
		String string = getStringAttr(node, attrName);
		if (string.isEmpty())
			return defaultValue;
		else
			return Integer.parseInt(string);
	}

	private String getStringAttr(Node node, String attrName, String defaultValue)
	{
		final NamedNodeMap attr = node.getAttributes();
		return getNodeValue(attr, attrName, defaultValue);
	}
	
	private String getStringAttr(Node node, String attrName)
	{
		final NamedNodeMap attr = node.getAttributes();
		return getNodeValue(attr, attrName, "");
	}
	
	private String getNodeValue(NamedNodeMap map, String key, String defaultValue) 
	{
		Node node = map.getNamedItem(key);	
		if (node != null) 
			return node.getNodeValue();
		else
			return defaultValue;
	}
	
	private String getNodeValue(NamedNodeMap map, String key) 
	{
		return getNodeValue(map, key, "");
	}
	
}