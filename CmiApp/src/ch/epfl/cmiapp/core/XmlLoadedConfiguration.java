package ch.epfl.cmiapp.core;

import ch.epfl.cmiapp.core.Configuration.*;

public class XmlLoadedConfiguration extends Configuration
{
	Builder configBuilder;
	
	public XmlLoadedConfiguration(org.w3c.dom.Node xmlNode, Equipment equipment)
	{		
		super(equipment);
		
		String nodeName = xmlNode.getNodeName();
		
		if (nodeName.equals("configuration"))
		{
			configBuilder = new Builder();
			parseConfigNode(xmlNode);
		}
	}
	
	private void parseConfigNode(final org.w3c.dom.Node configNode)
	{
		XmlExtractor configExtractor = new XmlExtractor(configNode);
		
		for (org.w3c.dom.Node childNode : configExtractor.childNodes())
		{
			String nodeName = childNode.getNodeName();	
			if (nodeName.equals("setting"))		parseSettingNode(childNode);
			else if (nodeName.equals("group"))	parseGroupNode(childNode);
		}
	}
	
	private Configuration.Setting parseSettingNode(final org.w3c.dom.Node settingNode)
	{
		XmlExtractor settingExtractor = new XmlExtractor(settingNode);
		Setting setting = configBuilder.createSetting();
		
		setting.currentValue = settingExtractor.getStringAttr("default", "0");
		setting.id 			 = settingExtractor.getStringAttr("id");
		setting.title 		 = settingExtractor.getStringAttr("title");
		setting.name 		 = settingExtractor.getStringAttr("name");
		setting.display		 = settingExtractor.getIntAttr("display", 1);
		setting.required 	 = settingExtractor.getEnumAttr(Node.Relevance.class, "required");
		
		for (org.w3c.dom.Node optionNode : settingExtractor.childNodes("option"))
		{
			XmlExtractor optionExtractor = new XmlExtractor(optionNode);
			
			Option option = new Option();
			option.value = 	optionExtractor.getStringAttr("value");
			option.title =  optionExtractor.getStringAttr("title");
			option.name  =  optionExtractor.getStringAttr("name");
			option.description = option.title;
			setting.options.add(option);
		}
		return setting;
	}
	
	private Configuration.Group parseGroupNode(final org.w3c.dom.Node groupNode)
	{
		XmlExtractor groupExtractor = new XmlExtractor(groupNode);
		
		Configuration.Group group = configBuilder.startGroup();
		group.id 		= groupExtractor.getStringAttr("id");
		group.title 	= groupExtractor.getStringAttr("title");
		group.required  = groupExtractor.getEnumAttr(Node.Relevance.class, "required");
		
		for(org.w3c.dom.Node settingNode : groupExtractor.childNodes("setting"))
			parseSettingNode(settingNode);
		
		configBuilder.endGroup();
		return group;
	}
	
}
