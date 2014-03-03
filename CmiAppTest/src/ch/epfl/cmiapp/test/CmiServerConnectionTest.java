package ch.epfl.cmiapp.test;

import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ch.epfl.cmiapp.util.CmiAccount;
import ch.epfl.cmiapp.util.CmiServerConnection;
import junit.framework.Assert;
import junit.framework.TestCase;

public class CmiServerConnectionTest extends TestCase
{
	CmiAccount account;
	CmiServerConnection server;
	
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
		
		System.out.println("Starting SSH tunnel");
		account.setupSshTunnel();
		
		System.out.println("Creating a server connection");
		server = account.getServerConnection();
		
		System.out.println("setup complete.");
		super.setUp();
	}
	
	public void testGetMainPage() throws IOException
	{
		System.out.println("loading page...");
		InputStream stream = server.getMainPage();
		
		System.out.println("parsing...");
		Document document = Jsoup.parse(stream, "ISO-8859-1", "");
		
		System.out.println("checking...");
		Element element = document.select("td[colspan=3]").first();
		System.out.print(element.ownText());
		
		boolean ok = element.ownText().contains("Welcome");
		Assert.assertTrue(ok);
		stream.close();
	}
	
	public void testGetMainPage_machId() throws IOException
	{
		InputStream stream = server.getMainPage("mach023");
		
		Document document = Jsoup.parse(stream, "ISO-8859-1", "");
		Element element = document.select("td[colspan=3]").first();
		System.out.print(element.ownText());
		
		boolean ok = element.ownText().contains("Welcome");
		Assert.assertTrue(ok);
		stream.close();
	}
	
	public void testGetMainPage_config() throws IOException
	{
		InputStream stream = server.getMainPage("mach023");
		
		Document document = Jsoup.parse(stream, "ISO-8859-1", "");
		Element element = document.select("td[colspan=3]").first();
		System.out.print(element.ownText());
		
		boolean ok = element.ownText().contains("Welcome");
		Assert.assertTrue(ok);
		stream.close();
	}
	
	public void testUserListPage() throws IOException
	{
		InputStream stream = server.getUserListPage();
		
		Document document = Jsoup.parse(stream, "ISO-8859-1", "");
		Element element = document.select("div").first();
		System.out.print(element.ownText());
		
		boolean ok = element.ownText().contains("users");		
		Assert.assertTrue(ok);
		stream.close();
	}
	
	public void testGetUserReservationPage() throws IOException
	{
		InputStream stream = server.getUserReservationPage();
		
		Document document = Jsoup.parse(stream, "ISO-8859-1", "");
		String title = document.title();
		System.out.print(title);
		
		boolean ok = title.contains("My réservations");
		Assert.assertTrue(ok);
		stream.close();
	}
	
	public void testGetNewsPage() throws IOException 
	{
		InputStream stream = server.getNewsPage("20");
		
		Document document = Jsoup.parse(stream, "ISO-8859-1", "");
		String title = document.title();
		System.out.print(title);
		
		boolean ok = title.contains("CMI NEWS");
		Assert.assertTrue(ok);
		stream.close();
	}
}
