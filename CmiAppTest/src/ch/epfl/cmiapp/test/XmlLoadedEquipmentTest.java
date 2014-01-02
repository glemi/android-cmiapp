package ch.epfl.cmiapp.test;

import java.io.*;

import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import android.test.AndroidTestCase;

import javax.xml.parsers.*;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.core.*;
import ch.epfl.cmiapp.core.XmlExtractor.ItemNotFoundException;
import ch.epfl.cmiapp.core.Configuration.*;

import static java.lang.System.*;

import junit.framework.TestCase;

public class XmlLoadedEquipmentTest extends AndroidTestCase
{
	private static String xmlfile = "/home/glemi/git/android-cmiapp/CmiApp/res/raw/cmitools.xml";
	
	public void testXmlLoadedEquipment() throws ItemNotFoundException, ParserConfigurationException, SAXException, IOException
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
				displayEquipment(equipment);
			}
		}
	}
	
	
	public void displayEquipment(Equipment eq)
	{
		
		out.println("::Equipment " + eq.getName() + " (" + eq.getMachId() + ") :::::::::::::::::::::::::::::::::::");
		out.println("  Full String: " + eq.getFullString());
		out.println("  Category: " + eq.getCategory());
		out.println("  Zone: " + eq.getZoneString());
		out.println("  Supplement: " + eq.getSupplement());
		out.println("  Slot Length: " + eq.getSlotLength());
		
		if (eq.isConfigurable()) displayConfiguration(eq.getConfig());
		
	}
	
	public void displayConfiguration(Configuration cf)
	{
		out.println("  :Configuration");
		
		for (Setting s : cf)
		{
			out.println("   ::Setting " + s.getTitle());
			out.println("     Cmi-Name: " + s.getName());
			out.println("     id: " + s.getId());
			out.println("     Value: " + s.getValue());
			out.println("     Required: " + s.getRequired().toString());
			out.println("     Displayed: " + s.getsDisplayed());
		}
	}
	
}
