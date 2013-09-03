package ch.epfl.cmiapp;

import junit.framework.TestCase;
import java.util.*;
import java.lang.*;

public class TestclassTest extends TestCase 
{

	private testclass dut;

	public void testLogon() 
	{
		dut = new testclass();
		dut.setCredentials("cnyffeler", "clemens");
		boolean success = dut.logon();
		
		assertTrue(success);
	}

	
	public void testUseres()
	{
		dut = new testclass();
		Set<String> names = dut.getUsers();
		
		for (String name : names)
		{
			System.out.print(name + "\n");
		}
		
		assertTrue(names.size() == 2);
	}
}
