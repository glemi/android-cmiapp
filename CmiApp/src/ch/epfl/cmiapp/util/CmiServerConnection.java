package ch.epfl.cmiapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.joda.time.LocalDate;

import ch.epfl.cmiapp.core.Configuration;

// Android HttpUrlConnection EOFException
// http://stackoverflow.com/questions/19258518/android-httpurlconnection-eofexception
public class CmiServerConnection
{
	private HttpURLConnection connection;
	private CmiSshTunnel sshTunnel;
	private CmiAccount account;
	
	private static final String defaultBaseUrl = "http://cmisrvm1.epfl.ch";
		
	static private String mainPageUrl =       "/reservation/reserv.php";
	static private String userListUrl =       "/spc/utilSB/include/tabUtilLimit.php?hauteur=800";
	static private String allReservPageUrl =  "/reservation/allreserv.php";
	static private String userReservPageUrl = "/reservation/myreserv.php";
	static private String newsPageUrl =       "/reservation/news/displayNews.php";
	
	public enum PageType 
	{
		MAIN_PAGE, MAIN_PAGE_RES, MAIN_PAGE_CONFIG_RES, USER_LIST, 
		DEBUG_USER_LIST, USER_RESERVATIONS_PAGE, ALL_RESERVATIONS_PAGE, 
		NEWS_PAGE, NEWS_PAGE_CONCAT;
		public int toInt() 	{ return this.ordinal(); }
		public static PageType fromInt(int value) { return PageType.values()[value]; }
	}
	
	public CmiServerConnection(CmiAccount account)
	{
		this.account = account;
		this.sshTunnel = new CmiSshTunnel(account);
	}
	
	public InputStream getPage(PageType pageType) throws IOException
	{
		String address = constructUrl(pageType);
		createHttpConnection(address);
		
		return connection.getInputStream();
	}
	
	public InputStream getMainPage() throws IOException
	{
		CmiHttpRequest request = setupConnection(PageType.MAIN_PAGE);
		request.setData("login", account.getUsername());
		request.setData("password", account.getPassword());
		request.send();
		return connection.getInputStream();
	}

	public InputStream getMainPage(String machId) throws IOException
	{
		return getMainPage(machId, 0);
	}
	
	public InputStream getMainPage(String machId, int dateOffset) throws IOException
	{
		CmiHttpRequest request = setupConnection(PageType.MAIN_PAGE_RES);
		request.setData("login", account.getUsername());
		request.setData("password", account.getPassword());
		request.setData("ID_Machine", machId);
		request.setData("mode", "reserve");
		request.setData("privilege", "simple");
		request.setData("sdays", Integer.toString(dateOffset));
		request.send();
		return connection.getInputStream();
	}
	
	public InputStream getMainPage(String machId, Configuration config) throws IOException
	{
		return getMainPage(machId, config, 0);
	}
	
	public InputStream getMainPage(String machId, Configuration config, int dateOffset) throws IOException
	{
		CmiHttpRequest request = setupConnection(PageType.MAIN_PAGE_CONFIG_RES);
		request.setData("login", account.getUsername());
		request.setData("password", account.getPassword());
		request.setData("ID_Machine", machId);
		request.setData("mode", "calendar"); // triggers configuration view
		request.setData("privilege", "simple");
		request.setData("sdays", Integer.toString(dateOffset));
		
		for (Configuration.Setting setting : config)
		{
			String settingId = setting.getId();
			String value = setting.getValue();
			request.setData(settingId, value);
		}
		request.send();
		return connection.getInputStream();
	}
	
	public InputStream getMainPage(String machId, Configuration.Values values) throws IOException
	{
		return getMainPage(machId, values, 0);
	}
	
	public InputStream getMainPage(String machId, Configuration.Values values, int dateOffset) throws IOException
	{
		CmiHttpRequest request = setupConnection(PageType.MAIN_PAGE_CONFIG_RES);
		request.setData("login", account.getUsername());
		request.setData("password", account.getPassword());
		request.setData("ID_Machine", machId);
		request.setData("mode", "calendar"); // triggers configuration view
		request.setData("privilege", "simple");
		
		for (Configuration.Values.Value value : values)
			request.setData(value.setting, value.option);
		
		request.send();
		return connection.getInputStream();
	}
	
	public InputStream getUserListPage() throws IOException
	{
		CmiHttpRequest request = setupConnection(PageType.USER_LIST);
		request.setCookie("CMI_user", "1");
		request.setCookie("droit", "0");
		request.send();
		return connection.getInputStream();
	}
	
	public InputStream getUserReservationPage() throws IOException
	{
		CmiHttpRequest request = setupConnection(PageType.USER_RESERVATIONS_PAGE);
		request.setData("ID_User", account.getUserId());
		request.setData("order1", "t2.NomMachine");
		request.setData("order2", "t3.DateRes");
		request.send();
		return connection.getInputStream();
	}
	
	public InputStream getAllReservationsPage(String machId, String startDate, String endDate) throws IOException
	{
		CmiHttpRequest request = setupConnection(PageType.ALL_RESERVATIONS_PAGE);
		request.setData("list1[]", machId);
		request.setData("ResDate", startDate);
		request.setData("ResDate2", endDate);
		request.setData("resbut", "Display");
		request.setData("order", "t3.DateRes,t2.NomMachine");
		request.send();
		return connection.getInputStream();
	}
	
	public InputStream getAllReservationsPage(String machId, String date) throws IOException
	{
		return getAllReservationsPage(machId, date, date);
	}
	
	public InputStream getAllReservationsPage(String machId, LocalDate date) throws IOException
	{
		return getAllReservationsPage(machId, date.toString("yyyy-MM-dd"));
	}
	
	public InputStream getAllReservationsPage(String machId, LocalDate startDate, LocalDate endDate) throws IOException
	{
		String start = startDate.toString("yyyy-MM-dd");
		String end = endDate.toString("yyyy-MM-dd");
		return getAllReservationsPage(machId, start, end);
	}
	
	public InputStream getAllReservationsPage(String machId) throws IOException
	{
		LocalDate today = new LocalDate();
		String date = today.toString("yyyy-MM-dd");
		return getAllReservationsPage(machId, date, date);
	}
	
	public InputStream getNewsPage(String newsId) throws IOException
	{
		CmiHttpRequest request = setupConnection(PageType.NEWS_PAGE);
		request.setData("news_id", newsId);
		request.send();
		return connection.getInputStream();
	}
	
	
	private CmiHttpRequest setupConnection(PageType pageType) throws IOException
	{
		String address = constructUrl(pageType);
		createHttpConnection(address);
		return new CmiHttpRequest(connection);
	}
	
	private String constructUrl(PageType pageType)
	{
		String baseUrl;
		/*if (sshTunnel.isActive())
			baseUrl = sshTunnel.getBaseUrl();
		else
			baseUrl = defaultBaseUrl;*/
		
		if (account.isSshTunnelConnected())
			baseUrl = sshTunnel.getBaseUrl();
		else
			baseUrl = defaultBaseUrl;

		switch (pageType)
		{
		case ALL_RESERVATIONS_PAGE:  return baseUrl + allReservPageUrl;
		case DEBUG_USER_LIST:
		case USER_LIST:				 return baseUrl + userListUrl;
		case MAIN_PAGE:
		case MAIN_PAGE_CONFIG_RES:
		case MAIN_PAGE_RES:			 return baseUrl + mainPageUrl;
		case NEWS_PAGE: 			 return baseUrl + newsPageUrl;
		case USER_RESERVATIONS_PAGE: return baseUrl + userReservPageUrl;
		}
		return null;
	}
	
	private void createHttpConnection(String urlAddress) throws IOException
	{
		try
		{
			URL url = new URL(urlAddress);
			this.connection = (HttpURLConnection) url.openConnection();
			this.connection.setDoOutput(true);
			// Next line is necessary to prevent EOFException
			this.connection.setRequestProperty("Connection", "close");
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			this.connection = null;
		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
	}
}



