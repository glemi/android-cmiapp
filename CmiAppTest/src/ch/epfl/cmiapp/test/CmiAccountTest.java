package ch.epfl.cmiapp.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jcraft.jsch.JSchException;

import junit.framework.Assert;
import ch.epfl.cmiapp.util.CmiAccount;
import ch.epfl.cmiapp.util.CmiServerConnection;
import android.test.AndroidTestCase;

public class CmiAccountTest extends AndroidTestCase
{
	CmiAccount account;
	
	@Override
	protected void setUp() throws Exception
	{
		account = new CmiAccount();
		
		account.setGasparId("cnyffele");
		account.setSciper("167382");
		account.setGasparPassword("Kaesekrainer123");
		account.setUserid("user1262");
		account.setUsername("cnyffeler");
		account.setPassword("clemens");
		//account.setupSshTunnel();
		
		super.setUp();
	}
	
	public void testSetupSshTunnel() throws JSchException
	{
		boolean ok = account.setupSshTunnel();
		Assert.assertTrue(ok);		
		
		account.closeSshTunnel();
		boolean closed = !account.isSshTunnelConnected();
		Assert.assertTrue(closed);
	}

	public void testGetServerConnection() throws IOException
	{
		CmiServerConnection server = account.getServerConnection();
		InputStream stream = server.getMainPage();
		
		//Document document = Jsoup.parse(stream, null, "https://cmisrvm1.epfl.ch");
		Document document = Jsoup.parse(stream, null, "");
		Element element = document.select("td[colspan=3]").first();
		System.out.print(element.ownText());
		boolean ok = element.ownText().contains("Welcome");
		Assert.assertTrue(ok);
		
	}

	
}
