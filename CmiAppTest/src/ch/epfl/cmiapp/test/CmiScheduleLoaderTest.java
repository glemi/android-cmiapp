package ch.epfl.cmiapp.test;

import org.joda.time.LocalDate;

import android.content.Context;
import ch.epfl.cmiapp.core.CmiSlot;
import ch.epfl.cmiapp.core.Schedule;
import ch.epfl.cmiapp.core.Schedule.SlotList;
import ch.epfl.cmiapp.util.CmiAccount;
import ch.epfl.cmiapp.util.CmiScheduleLoader;
import ch.epfl.cmiapp.util.EquipmentManager;
import junit.framework.Assert;

public class CmiScheduleLoaderTest extends android.test.LoaderTestCase	 
{
	EquipmentManager em = null;
	CmiAccount account = null;
	
	@Override
	protected void setUp() throws Exception {
		EquipmentManager.load(this.getContext());
		
		account = new CmiAccount();
		account.setGasparId("cnyffele");
		account.setSciper("167382");
		account.setGasparPassword("2branchGraphene");
		account.setUserid("user1262");
		account.setUsername("cnyffeler");
		account.setPassword("clemens");
		account.setActive();
		
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}

	public void testLoadSchedule()
	{
		Context context = this.getContext();
		
		CmiScheduleLoader loader = new CmiScheduleLoader(context);
		loader.setMachId("mach118");
		//loader.loadInBackground();
		Schedule schedule = super.getLoaderResultSynchronously(loader);
		printSchedule(schedule);
		Assert.assertNotNull(schedule);
		Assert.assertTrue(schedule.getDayCount()>1); 
		
		schedule = super.getLoaderResultSynchronously(loader);
		printSchedule(schedule);
		Assert.assertNotNull(schedule);
		Assert.assertTrue(schedule.getDayCount()>1); 
		
	}
	
	private void printSchedule(Schedule schedule)
	{
		int nDays = schedule.getDayCount();
		int chunkSize = 4;
		
		for (int iChunk = 0; iChunk <= nDays/chunkSize; iChunk++)
		{
			int start = iChunk*chunkSize;
			int end = start + chunkSize -1;
			end = end >= nDays ? nDays-1 : end;
			printScheduleChunk(schedule, start, end);
		}
	}
	
	private void printScheduleChunk(Schedule schedule, int start, int end)
	{
		int nSlots = schedule.getSlotsPerDayCount();
		String row = "";
		
		for (int iDate = start; iDate <= end; iDate++)
		{
			LocalDate date = schedule.getDate(iDate);
			row += String.format("%15s ", date.toString("EEE"));
		}
		System.out.println(row);
		
		for (int iSlot = 0; iSlot < nSlots; iSlot++)
		{
			row = "";
			for (int iDate = start; iDate <= end; iDate++)
			{
				SlotList slots = schedule.getSlots(iDate);
				CmiSlot slot = slots.get(iSlot);
				
				row += String.format("%15s ", slot.toString());
				//row += String.format("%15s ", slot.configValues.toString());
			}
			System.out.println(row);
		}
	}
}
