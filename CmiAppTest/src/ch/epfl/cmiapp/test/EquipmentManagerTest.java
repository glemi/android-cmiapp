package ch.epfl.cmiapp.test;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import junit.framework.Assert;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.util.EquipmentManager;
import ch.epfl.cmiapp.util.EquipmentManager.InventoryLoadException;
import android.test.AndroidTestCase;

public class EquipmentManagerTest extends AndroidTestCase
{
	EquipmentManager em = null;
	
	
	@Override
	protected void setUp()	throws Exception
	{
		em = new EquipmentManager(super.getContext());
		super.setUp();
	}
	
	public void testEquipmentManager() throws InventoryLoadException
	{
		Assert.assertTrue(em.load());
		
		
		for (String machid : machids)
		{
			Equipment eq = em.getInventory().get(machid);
			Assert.assertEquals(machid, eq.getMachId());
			if (machid.equals(eq.getMachId())) System.out.println(machid + " OK");
			else System.out.println(machid + " FAIL (returned " + eq.getMachId() + ")");
		}
	}
	
	private static String[] machids = { "mach002", "mach003", "mach004", "mach005", "mach006", "mach007", "mach008", "mach009", "mach010", "mach011", "mach012", "mach013", "mach014", "mach015", "mach017", "mach018", "mach019", "mach020", "mach022", "mach023", "mach024", "mach025", "mach026", "mach027", "mach028", "mach029", "mach030", "mach031", "mach033", "mach034", "mach035", "mach036", "mach039", "mach040", "mach044", "mach045", "mach046", "mach047", "mach048", "mach053", "mach054", "mach055", "mach058", "mach116", "mach118", "mach119", "mach121", "mach124", "mach125", "mach126", "mach127", "mach128", "mach129", "mach130", "mach131", "mach132", "mach133", "mach134", "mach135", "mach136", "mach137", "mach138", "mach139", "mach140", "mach141", "mach142", "mach143", "mach144", "mach145", "mach146", "mach147", "mach148", "mach149", "mach150", "mach151", "mach152", "mach153", "mach154", "mach155", "mach156", "mach157", "mach158", "mach159", "mach160", "mach161", "mach162", "mach163", "mach164", "mach165", "mach166", "mach167", "mach168", "mach169", "mach170", "mach171", "mach172", "mach173"}; 
	
	public void testFindEquipment() throws IOException
	{
		String[] strings = loadCmiToolStrings();
		
		for (String string : strings)
		{
			Equipment eq = em.getInventory().find(string);
			
			if (eq != null)
				System.out.println(eq.getMachId() + " found : " + string);
			else
				System.out.println("NOT found : " + string);
			
			//Assert.assertNotNull(eq);
		}
	}
	
	static private String baseUrl = "http://cmisrvm1.epfl.ch";
	static private String mainPageUrl = baseUrl + "/reservation/reserv.php";
	static private String userListUrl = "http://cmisrv1.epfl.ch/spc/utilSB/include/tabUtilLimit.php?hauteur=800";
	static private String allReservPageUrl = baseUrl + "/reservation/allreserv.php";
	static private String userReservPageUrl = baseUrl + "/reservation/myreserv.php";
	static private String newsPageUrl = baseUrl + "/reservation/news/displayNews.php";
	
	private String[] loadCmiToolStrings() throws IOException
	{
		Connection connection;
		connection = Jsoup.connect(allReservPageUrl);
		Document document = connection.post();
		
		Elements optionEls = document.select("select>option");
		int count = optionEls.size();
		
		String[] strings = new String[count];
		
		for (int index = 0; index < count; index++)
		{
			strings[index] = optionEls.get(index).ownText();
			//System.out.println("machine text (" + index + ") = " + strings[index]);
		}
		
		return strings;
	}
	
}
