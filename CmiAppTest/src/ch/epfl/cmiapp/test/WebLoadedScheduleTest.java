package ch.epfl.cmiapp.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.test.AndroidTestCase;

import junit.framework.TestCase;
import ch.epfl.cmiapp.core.CmiSlot;
import ch.epfl.cmiapp.core.Configuration;
import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.Inventory;
import ch.epfl.cmiapp.core.Schedule;
import ch.epfl.cmiapp.core.Schedule.SlotList;
import ch.epfl.cmiapp.core.WebLoadedSchedule;
import ch.epfl.cmiapp.core.XmlLoadedEquipment;
import ch.epfl.cmiapp.json.JsonStreamReader;
import ch.epfl.cmiapp.json.JsonableSchedule;
import ch.epfl.cmiapp.util.CmiAccount;
import ch.epfl.cmiapp.util.CmiServerConnection;
import ch.epfl.cmiapp.util.EquipmentManager;


public class WebLoadedScheduleTest extends AndroidTestCase
{
	CmiAccount account;
	CmiServerConnection server;
	String machId = "mach136"; // Lab600
	Equipment equipment;
	Inventory inventory;
	EquipmentManager equipmentManager;
	
	protected void setUp() throws Exception 
	{
		System.out.println("Setting up account data");
		account = new CmiAccount();
		account.setGasparId("cnyffele");
		account.setSciper("167382");
		account.setGasparPassword("2branchGraphene");
		account.setUserid("user1262");
		account.setUsername("cnyffeler");
		account.setPassword("clemens");
		
		//System.out.println("Starting SSH tunnel");
		//account.setupSshTunnel();
		
		System.out.println("Creating a server connection");
		server = account.getServerConnection();
		
		System.out.println("setup complete.");
		
		equipmentManager = new EquipmentManager(super.getContext());
		equipmentManager.load();
		equipment = equipmentManager.getInventory().get(machId);
		
		super.setUp();
	}
	
	
	public void testWebLoadedScheduleConfig() throws IOException
	{
		Configuration.Values config = new Configuration.Values(equipment);
		config.set("Category", "HHN");
		config.set("E-beam #3", "Au");
		
		InputStream stream = server.getMainPage(machId, config);
		WebLoadedSchedule schedule = new WebLoadedSchedule(stream, equipment);
		printSchedule(schedule);
	}
	
	public void testWebLoadedSchedule() throws IOException
	{
		InputStream stream = server.getMainPage(machId);
		WebLoadedSchedule schedule = new WebLoadedSchedule(stream, equipment);
		printSchedule(schedule);
	}
	
	public void testWebLoadedScheduleAllReserv() throws IOException
	{
		InputStream stream = server.getAllReservationsPage(machId);
		WebLoadedSchedule schedule = new WebLoadedSchedule(stream, equipment);
		printSchedule(schedule);
	}
	
	public void testWebLoadedScheduleMerge() throws IOException
	{
		InputStream stream1 = server.getMainPage(machId);
		WebLoadedSchedule schedule1 = new WebLoadedSchedule(stream1, equipment);
		
		InputStream stream2 = server.getAllReservationsPage(machId);
		WebLoadedSchedule schedule2 = new WebLoadedSchedule(stream2, equipment);
		
		Schedule schedule3 = Schedule.merge(schedule1, schedule2);
		
		printSchedule(schedule3);
	}
	
	public void testConsecutiveMerge() throws IOException
	{
		InputStream stream1 = server.getMainPage(machId);
		WebLoadedSchedule schedule1 = new WebLoadedSchedule(stream1, equipment);
		
		InputStream stream2 = server.getMainPage(machId, 15);
		WebLoadedSchedule schedule2 = new WebLoadedSchedule(stream2, equipment);
		
		InputStream stream3 = server.getMainPage(machId, 7);
		WebLoadedSchedule schedule3 = new WebLoadedSchedule(stream3, equipment);
		
		Schedule schedule = Schedule.merge(schedule1, schedule2);
		schedule = Schedule.merge(schedule, schedule3);
		
		printSchedule(schedule);
	}
	
	public void testJasonizeSchedule() throws IOException, JSONException
	{
		InputStream stream = server.getMainPage(machId);
		WebLoadedSchedule schedule = new WebLoadedSchedule(stream, equipment);
		JsonableSchedule schedule1 = new JsonableSchedule(schedule);
		JSONObject json = schedule1.serialize();
		System.out.print(json.toString(4));
	}
	    
	public void testJasonizeAndReload() throws IOException, JSONException
	{
		InputStream stream = server.getMainPage(machId);
		WebLoadedSchedule schedule = new WebLoadedSchedule(stream, equipment);
		JsonableSchedule schedule1 = new JsonableSchedule(schedule);
		JSONObject json1 = schedule1.serialize();
		
		System.out.print(json1.toString(4));
		
		String filename = "jasontestfile";
		FileOutputStream outstream = super.getContext().openFileOutput(filename, Context.MODE_PRIVATE); 
		outstream.write(json1.toString().getBytes());
		outstream.close();
		
		FileInputStream instream = super.getContext().openFileInput(filename);
		JsonStreamReader reader = new JsonStreamReader(instream);
		JSONObject json2 = reader.readJason();
		
	    Schedule reloadedSchedule = new JsonableSchedule(json2, equipment);
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
	
	@Override
	protected void tearDown() throws Exception
	{
		account.closeSshTunnel();
		super.tearDown();
	}
	
}
