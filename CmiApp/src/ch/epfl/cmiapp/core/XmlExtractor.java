package ch.epfl.cmiapp.core;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import android.util.Log;

public class XmlExtractor
{
	private Node node;
	private NamedNodeMap attributes;
	
	public class ItemNotFoundException extends XmlReadoutException
	{
		public ItemNotFoundException(String item) 
		{
			super("Could not find item '" + item + "' within " + node.getNodeName()); 
		}
	}
	
	public class InvalidDataException extends XmlReadoutException
	{
		public InvalidDataException(String item, String data, String datatype) 
		{
			super("Invalid value of node '" + item + "'; Cannot convert '" + data + "' to " + datatype);
		} 
	}
	
	public XmlExtractor(Node node)
	{
		this.node = node;
		this.attributes = node.getAttributes();
	}
	
	public Iterable<Node> childNodes(String tagName)
	{
		if (node instanceof Element)
		{
			Element element = (Element)node;
			NodeList nodes = element.getElementsByTagName(tagName);
			return getIterable(nodes);
		}
		else
		{
			return getIterable(null);
		}
	}
	
	public Iterable<Node> childNodes()
	{
		return getIterable(node.getChildNodes());
	}
	
	private Iterable<Node> getIterable(final NodeList nodes)
	{
		return new Iterable<Node>() {
			public Iterator<Node> iterator() { return new NodeIterator(nodes); }
		};
	}
	
	public int getIntAttr(String attrName) throws ItemNotFoundException
	{
		String string = getStringAttr(attrName);
		return Integer.parseInt(string);
	}
	
	public int getIntAttr(String attrName, int defaultValue)
	{
		try 
		{
			String string = getStringAttr(attrName);
			return Integer.parseInt(string);
		}
		catch (ItemNotFoundException e)
		{
			return defaultValue;
		}
		catch (NumberFormatException e)
		{
			Log.d("XmlExtractor.getIntAttr", "Invalid Attribute Format: using default Value! Please investigate!", e);
			return defaultValue;
		}
	}

	public String getStringAttr(String attrName, String defaultValue)
	{
		return getNodeValue(attributes, attrName, defaultValue);
	}
	
	public String getStringAttr(String attrName) throws ItemNotFoundException
	{
		return getNodeValue(attributes, attrName);
	}
	
	public <T extends Enum<T>> T getEnumAttr(Class<T> enumType, String attrName) throws InvalidDataException, ItemNotFoundException 
	{
		String string = getStringAttr(attrName);
		try { return Enum.valueOf(enumType, string); }
		catch (Exception e)
		{
			throw new InvalidDataException(attrName, string, enumType.getName());
		}
	}
	
	public <T extends Enum<T>> T getEnumAttr(Class<T> enumType, String attrName, T defaultValue) throws ItemNotFoundException
	{
		try 
		{
			String string = getStringAttr(attrName).toUpperCase();
			T enumvalue = Enum.valueOf(enumType, string);
			if (enumvalue == null)
				return defaultValue;
			else
				return enumvalue;
		}
		catch (ItemNotFoundException e)
		{
			Log.d("XmlExtractor.getEnumAttr", e.getMessage());
			return defaultValue;
		}
	}

	private String getNodeValue(NamedNodeMap map, String key, String defaultValue) 
	{
		Node node = map.getNamedItem(key);	
		if (node != null) 
			return node.getNodeValue();
		else
			return defaultValue;
	}
	
	private String getNodeValue(NamedNodeMap map, String key) throws ItemNotFoundException 
	{
		Node node = map.getNamedItem(key);	
		if (node != null) 
			return node.getNodeValue();
		else
			throw new ItemNotFoundException(key);
	}
	

	public class NodeIterator implements Iterator<Node>
	{
		final NodeList list;
		int currentIndex = 0;
		
		public NodeIterator(NodeList nodes)
		{
			list = nodes;
		}
		
		public boolean hasNext()
		{
			if (list != null)
				return list.getLength() > currentIndex;
			else
				return false;
		}

		public Node next()
		{
			if (list != null)
				return list.item(currentIndex++);
			else
				return null;
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
}
