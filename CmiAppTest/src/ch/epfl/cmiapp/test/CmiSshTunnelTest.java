package ch.epfl.cmiapp.test;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jcraft.jsch.JSchException;

import junit.framework.Assert;
import junit.framework.TestCase;
import ch.epfl.cmiapp.util.CmiSshTunnel;

public class CmiSshTunnelTest extends TestCase
{
	CmiSshTunnel tunnel;
	
	static private String baseUrl = "http://localhost:9080";
	static private String mainPageUrl = baseUrl + "/reservation/reserv.php";
	
	
	@Override
	protected void setUp() 	throws Exception
	{
		tunnel = new CmiSshTunnel(); 
		super.setUp();
	}
	
	public void testEstablish() throws Exception
	{
		tunnel.establish();
		
		Document document = null; 
		Connection connection;
		
		connection = Jsoup.connect(mainPageUrl);
		connection.header("HTTP-version", "HTTP/1.1");
		connection.data("login", "cnyffeler");
		connection.data("password", "clemens");
		document = connection.post();
		Element element = document.select("td[colspan=3]").first();
		System.out.print(element.ownText());
		
		boolean ok = element.ownText().contains("Welcome");
		Assert.assertTrue(ok); 
	}
}
