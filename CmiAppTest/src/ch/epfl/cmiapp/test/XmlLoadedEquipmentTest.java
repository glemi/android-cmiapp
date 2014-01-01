package ch.epfl.cmiapp.test;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import android.test.AndroidTestCase;

import javax.xml.parsers.*;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.core.*;

import junit.framework.TestCase;

public class XmlLoadedEquipmentTest extends AndroidTestCase
{
	private static String xmlfile = "/home/glemi/git/android-cmiapp/CmiApp/res/raw/cmitools.xml";
	
	public void testXmlLoadedEquipment() throws ParserConfigurationException, SAXException, IOException
	{
		InputStream stream = super.getContext().getResources().openRawResource(R.raw.cmitools);
		
		//FileInputStream stream = new FileInputStream(xmlfile);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(stream, null);
		Element rootElement = doc.getDocumentElement();
		rootElement.normalize();
		
		NodeList nodes = rootElement.getChildNodes();
		
		for(int index = 0; index < nodes.getLength(); index++)
		{
			Node xmlNode = nodes.item(index); 
			String nodeName = xmlNode.getNodeName();
			
			if (nodeName.equals("cmitool"))
			{
				Equipment equipment = new XmlLoadedEquipment(xmlNode);
				String key = equipment.getMachId();
			}
		}
	}
	
}
