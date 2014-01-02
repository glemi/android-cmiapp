package ch.epfl.cmiapp.test;

import junit.framework.Assert;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.util.EquipmentManager;
import ch.epfl.cmiapp.util.EquipmentManager.InventoryLoadException;
import android.test.AndroidTestCase;

public class EquipmentManagerTest extends AndroidTestCase
{
	public void testEquipmentManager() throws InventoryLoadException
	{
		EquipmentManager.load(super.getContext());
		
		EquipmentManager em = new EquipmentManager(super.getContext());
		
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
	
}
