package ch.epfl.cmiapp.core;

import org.w3c.dom.Node;

import ch.epfl.cmiapp.core.XmlExtractor.ItemNotFoundException;


public class XmlLoadedEquipment extends Equipment
{

	public XmlLoadedEquipment(org.w3c.dom.Node xmlNode) throws ItemNotFoundException
	{
		assert(xmlNode.getNodeName().equals("cmitool"));
		XmlExtractor extractor = new XmlExtractor(xmlNode);
		
		this.machId 	= extractor.getStringAttr("machId");
		this.name       = extractor.getStringAttr("name");
		this.zone   	= extractor.getIntAttr("zone");
		this.slotLength = extractor.getIntAttr("slotLength");
		this.fullString = extractor.getStringAttr("fullString");
		this.supplement = extractor.getStringAttr("description");
		
		if (xmlNode.hasChildNodes())
		{
			Node configNode = xmlNode.getChildNodes().item(1);
			assert(configNode.getNodeName().equals("configuration"));
			config = new XmlLoadedConfiguration(configNode, this);
			isConfigurable = true;
		}
		else
		{
			config = new Configuration(this);
		}
	}
}
