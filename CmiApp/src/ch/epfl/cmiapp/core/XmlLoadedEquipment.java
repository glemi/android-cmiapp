package ch.epfl.cmiapp.core;

import org.w3c.dom.Node;

public class XmlLoadedEquipment extends Equipment
{

	public XmlLoadedEquipment(org.w3c.dom.Node xmlNode)
	{
		assert(xmlNode.getNodeName().equals("cmitool"));
		XmlExtractor extractor = new XmlExtractor(xmlNode);
		
		this.machId 	= extractor.getStringAttr("machId");
		this.name       = extractor.getStringAttr("name");
		this.zone   	= extractor.getIntAttr("zone");
		this.slotLength = extractor.getIntAttr("slotLength");
		this.fullString = extractor.getStringAttr("fullString");
		
		if (xmlNode.hasChildNodes())
		{
			Node configNode = xmlNode.getChildNodes().item(1);
			assert(configNode.getNodeName().equals("configuration"));
			config = new XmlLoadedConfiguration(configNode);
			isConfigurable = true;
		}
		else
		{
			config = new Configuration();
		}
	}
}
