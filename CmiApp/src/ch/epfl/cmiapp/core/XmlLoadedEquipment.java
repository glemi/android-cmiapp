package ch.epfl.cmiapp.core;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XmlLoadedEquipment extends Equipment
{

	public XmlLoadedEquipment(org.w3c.dom.Node xmlNode)
	{
		assert(xmlNode.getNodeName().equals("cmitool"));
		final NamedNodeMap attr = xmlNode.getAttributes();
		
		String zoneStr, slotLengthStr, description;
		
		machId 			= getNodeValue(attr, "machId");
		zoneStr 		= getNodeValue(attr, "zone");
		slotLengthStr 	= getNodeValue(attr, "slotLength");
		name   			= getNodeValue(attr, "name");
		description 	= getNodeValue(attr, "description");
		fullString 		= getNodeValue(attr, "fullString");
		
		
		if (xmlNode.hasChildNodes())
		{
			Node configNode = toolNode.getChildNodes().item(1);
			assert(configNode.getNodeName().equals("configuration"));
			config = new XmlLoadedConfiguration(configNode);
			isConfigurable = true;
		}
		else
			config = new Configuration();
		
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
