package ch.epfl.cmiapp.core;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import ch.epfl.cmiapp.core.Configuration;

import android.util.Log;

public class XmlLoadedConfiguration extends Configuration
{
	public XmlLoadedConfiguration(org.w3c.dom.Node xmlNode)
	{
		String nodeName = xmlNode.getNodeName();
		
		if (nodeName.equals("setting"))
		{
			Setting setting = parseSettingNode(xmlNode);
			settings.add(setting);
		}
		else if (nodeName.equals("group"))
		{
			Group group = parseGroupNode(node);
			settings.addAll(group.settings);
		}
	}
	
	private Configuration.Setting parseSettingNode(final Node settingNode)
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
	
}
