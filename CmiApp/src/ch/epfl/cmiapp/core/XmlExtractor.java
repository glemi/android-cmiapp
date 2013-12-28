package ch.epfl.cmiapp.core;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

public class XmlExtractor
{
	private Node node;
	private NamedNodeMap attributes;
	
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
	
	public int getIntAttr(String attrName)
	{
		String string = getStringAttr(attrName);
		return Integer.parseInt(string);
	}
	
	public int getIntAttr(String attrName, int defaultValue)
	{
		String string = getStringAttr(attrName);
		if (string.isEmpty())
			return defaultValue;
		else
			return Integer.parseInt(string);
	}

	public String getStringAttr(String attrName, String defaultValue)
	{
		return getNodeValue(attributes, attrName, defaultValue);
	}
	
	public String getStringAttr(String attrName)
	{
		return getNodeValue(attributes, attrName, "");
	}
	
	public <T extends Enum<T>> T getEnumAttr(Class<T> enumType, String attrName)
	{
		String string = getStringAttr(attrName);
		return Enum.valueOf(enumType, string);
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
				return list.getLength() > currentIndex + 1;
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
