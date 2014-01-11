package ch.epfl.cmiapp.test;

import java.io.IOException;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import junit.framework.Assert;
import ch.epfl.cmiapp.core.Configuration;
import ch.epfl.cmiapp.core.Configuration.Node;
import ch.epfl.cmiapp.core.Configuration.Node.Relevance;
import ch.epfl.cmiapp.core.Configuration.Option;
import ch.epfl.cmiapp.core.Configuration.Setting;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.util.EquipmentManager;
import ch.epfl.cmiapp.util.EquipmentManager.InventoryLoadException;
import android.test.AndroidTestCase;
import android.util.Log;

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
	
	
	public void testIsValid()
	{
		for (String machid : machids)
		{
			Equipment eq = em.getInventory().get(machid);
			
			if (eq.isConfigurable())
			{
				Configuration config = eq.getConfig();
				ConfigGenerator gen = new ConfigGenerator(config);
				ConfigValidator val = new ConfigValidator(eq);
				
				String header = "";
				for (Setting setting : config)
					header += String.format("%10s", setting.getName());
				header += String.format("%10s", "Reference");
				header += String.format("%10s", "Actual");
				
				System.out.println(header);
				
				String row;
				
				while (gen.hasNext())
				{
					gen.increment();
					Assert.assertTrue(val.check());
				}
				
				System.out.println("Configurable tool " + eq.getName() + " - validation test successful!");
			}
			
			Assert.assertEquals(machid, eq.getMachId());
			if (machid.equals(eq.getMachId())) System.out.println(machid + " OK");
			else System.out.println(machid + " FAIL (returned " + eq.getMachId() + ")");
		}
	}
	
	private static class ConfigGenerator
	{
		private Configuration config;
		private int combinations = 0;
		private int index = 0;
		
		public ConfigGenerator(Configuration config)
		{
			this.config = config;
			int count = 1;
			for(Setting setting : config)
				count *= setting.getOptions().size();
			combinations = count;
			System.out.println("ConfigGenerator :: Combinations: " + count);
			combinations = count > 1000 ? 1000 : count;
		}
		
		public boolean hasNext()
		{
			return index < combinations; 
		}
		
		public void increment()
		{
			int n = index++;
			for(Setting setting : config)
			{
				int k = setting.getOptions().size();
				int i = n % k;
				
				Option option = setting.getOptions().get(i);
				setting.change(option);
				
				n /= k;
			}
		}
	}
	
	private static class ConfigValidator
	{
		private Configuration config;
		private Equipment eqpt;
		
		public ConfigValidator(Equipment equipment)
		{
			eqpt = equipment;
			config = eqpt.getConfig();
		}
		 
		public boolean check()
		{
			boolean test = config.isValid();
			boolean ref  = referenceValidation();
			boolean result = (test == ref);
			
			String row = "";
			for (Setting setting : config)
				row += String.format("%10s", setting.getValue());
			
			row += String.format("%10s", ref ? "OK" : " -");
			row += String.format("%10s", test ? "OK" : " -");
			System.out.println(row);
			
			
			if (!result)
			{
				for(Setting setting : config)
				{
					System.out.println(setting.getTitle() + " = " + setting.getCurrent().title);
				}
				System.out.println("Config object says : " + (test ? "OK" : "INVALID"));
				System.out.println("Reference implementation says : " + (ref ? "OK" : "INVALID"));
			}
			
			return result;
		}
		
		public boolean referenceValidation()
		{
			if (eqpt.getMachId().equals("mach022")) // BAS450
			{
				boolean heatLamps = checkSetting("list0");
				boolean meissner  = checkSetting("list1");
				boolean cleaning  = checkSetting("list2");
				boolean rfTarget  = checkSetting("list3");
				boolean dc1Target = checkSetting("list4");
				boolean dc2Target = checkSetting("list5");
				return heatLamps && meissner && cleaning && (rfTarget || dc1Target || dc2Target);
			}
			else if (eqpt.getMachId().equals("mach023")) // EVA600
			{
				boolean ebeam1 = checkSetting("list0");
				boolean ebeam2 = checkSetting("list1");
				boolean ebeam3 = checkSetting("list2");
				boolean ebeam4 = checkSetting("list3");
				boolean joule1 = checkSetting("list4");
				boolean joule2 = checkSetting("list5");
				return ebeam1 || ebeam2 || ebeam3 || ebeam4 || joule1 || joule2;
			}
			else if (eqpt.getMachId().equals("mach025")) // Desaules Wet bench
			{
				boolean bath = checkSetting("list0");
				return bath;
			}
			else if (eqpt.getMachId().equals("mach044")) // Süss SB6 Anodic Bonder
			{
				boolean fixature = checkSetting("list0");
				boolean proctype = checkSetting("list1");
				return fixature && proctype;
			}
			else if (eqpt.getMachId().equals("mach118")) // Spider
			{
				boolean wafersize = checkSetting("list0");
				boolean pm1Target = checkSetting("list1");
				boolean pm1Temp   = checkSetting("list2");
				boolean pm2Target = checkSetting("list3");
				boolean pm2Temp   = checkSetting("list4");
				boolean pm3Target = checkSetting("list5");
				boolean pm3Temp   = checkSetting("list6");
				boolean pm4Target = checkSetting("list7");
				boolean pm4Temp   = checkSetting("list8");
				return (wafersize && ( pm1Target && pm1Temp || pm2Target && pm2Temp || pm3Target && pm3Temp || pm4Target && pm4Temp));
			}
			else if (eqpt.getMachId().equals("mach126")) // EVG150
			{
				boolean wafersize    = checkSetting("list0");
				boolean syringe      = checkSetting("list1");
				boolean developer    = checkSetting("list2");
				boolean resist1      = checkSetting("list3");
				boolean resist2      = checkSetting("list4");
				return wafersize && (syringe || developer || resist1 || resist2);
			}
			else if (eqpt.getMachId().equals("mach131")) // sanding machine
			{
				boolean sable = checkSetting("list0");
				return sable;
			}
			else if (eqpt.getMachId().equals("mach136")) // LAB600
			{
				boolean category   = checkSetting("list0"); 
				boolean crucible1  = checkSetting("list1");
				boolean crucible2  = checkSetting("list2");
				boolean crucible3  = checkSetting("list3");
				boolean crucible4  = checkSetting("list4");
				boolean crucible5  = checkSetting("list5");
				boolean crucible6  = checkSetting("list6");
				return category && (crucible1 || crucible2 || crucible3 || crucible4 || crucible5 || crucible6);
			}                     
			else if (eqpt.getMachId().equals("mach138")) // Sawatec LSM200 
			{
				boolean chemistry = checkSetting("list0");
				return chemistry;
			}
			else if (eqpt.getMachId().equals("mach155")) // Beneq TFS200 ALD
			{
				boolean run        = checkSetting("list0");
				boolean liquidsrc1 = checkSetting("list1");
				boolean liquidsrc2 = checkSetting("list2");
				boolean liquidsrc3 = checkSetting("list3");
				boolean hotsource1 = checkSetting("list4");
				boolean hotsource2 = checkSetting("list5");
				return run && (liquidsrc1 || liquidsrc2 || liquidsrc3 || hotsource1 || hotsource2);
			}
				
			return true;
		}
		
		private boolean checkSetting(String id)
		{
			Setting setting = config.getSetting(id);
			Assert.assertNotNull(setting);
			
			return !setting.getValue().equals("0");
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
