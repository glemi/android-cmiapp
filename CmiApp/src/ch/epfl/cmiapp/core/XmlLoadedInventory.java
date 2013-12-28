package ch.epfl.cmiapp.core;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlLoadedInventory extends Inventory
{
	public XmlLoadedInventory(Node xmlRootNode)
	{
		xmlLoad(xmlRootNode);
	}
	
	public void xmlLoad(Node xmlRootNode)
	{
		NodeList nodes = xmlRootNode.getChildNodes();
		
		for(int index = 0; index < nodes.getLength(); index++)
		{
			Node xmlNode = nodes.item(index); 
			String nodeName = xmlNode.getNodeName();
			
			if (nodeName.equals("cmitool"))
			{
				Equipment equipment = new XmlLoadedEquipment(xmlNode);
				String key = equipment.getMachId();
				inventory.put(key, equipment);
			}
		}
	}
}
