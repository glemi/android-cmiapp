package ch.epfl.cmiapp.util;

import java.lang.Exception;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import org.joda.time.LocalDate;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.core.Configuration;

import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

/* How to correctly implement custom loaders! 
 * Very important: check the part on source data monitoring and updating.
 * http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
 * 
 *  http://www.grokkingandroid.com/using-loaders-in-android/
 */


public class CmiLoader extends AsyncTaskLoader<Document> 
{
	public enum PageType 
	{
		MAIN_PAGE, MAIN_PAGE_RES, MAIN_PAGE_CONFIG_RES, USER_LIST, 
		DEBUG_USER_LIST, USER_RESERVATIONS_PAGE, ALL_RESERVATIONS_PAGE, 
		NEWS_PAGE, NEWS_PAGE_CONCAT;
		public int toInt() 	{ return this.ordinal(); }
		public static PageType fromInt(int value) { return PageType.values()[value]; }
	}
	
	private class ReloadTask extends TimerTask
	{
		@Override
		public void run()
		{
			if (isStarted()) forceLoad();			
		}
	}
	
	static private String baseUrl = "http://cmisrvm1.epfl.ch";
	static private String mainPageUrl = baseUrl + "/reservation/reserv.php";
	static private String userListUrl = "http://cmisrv1.epfl.ch/spc/utilSB/include/tabUtilLimit.php?hauteur=800";
	static private String allReservPageUrl = baseUrl + "/reservation/allreserv.php";
	static private String userReservPageUrl = baseUrl + "/reservation/myreserv.php";
	static private String newsPageUrl = baseUrl + "/reservation/news/displayNews.php";
	
	private Document cmiPage = null;
	private PageType dedication;
	
	private boolean enableCachedData = true;
	
	private Timer reloadTimer = new Timer();
	private long reloadInterval = 0; // interval in seconds; 0 = don't reload
	
	private String username;
	private String password;
	private String userId;
	
	private String dateStart;
	private String dateEnd;
	
	private String newsId;
	private String machId;
	private Configuration config;
	
	private int newsCap = 5;
	
	private Exception lastException = null;

	
	public CmiLoader(Context context, PageType dedication) 
	{
		super(context); // allows to retrieve the context form AsyncTaskLoader using getContext
		this.dedication = dedication;
		
		SharedPreferences preferences = context.getSharedPreferences("CMI_CREDENTIALS", Context.MODE_PRIVATE);
		username = preferences.getString("CMI_USERNAME", null);
		password = preferences.getString("CMI_PASSWORD", null);
		userId   = preferences.getString("CMI_USERID", null);
		
		setDate(new LocalDate());// today 
	}
	
	public void setCredentials(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	public void setDate(LocalDate date)
	{
		setDateRange(date, date);
	}
	
	public void setConfig(Configuration config)
	{
		this.config = config;
	}
	
	public void setNewsCap(int maxItems)
	{
		this.newsCap = maxItems;  
	}
	
	public void setEnableCachedData(boolean enable)
	{
		this.enableCachedData = enable;
	}
	
	public void setReloadInterval(int seconds)
	{
		this.reloadInterval = seconds;
	}
	
	public void setDateRange(LocalDate dateStart, LocalDate dateEnd)
	{
		this.dateStart = dateStart.toString("yyyy-MM-dd");
		this.dateEnd   = dateEnd.toString("yyyy-MM-dd");
	}
	
	public void setMachId(String machId)
	{
		this.machId = machId;
	}
	
	public void setNewsId(String newsId)
	{
		this.newsId = newsId;
	}
	
	
	@Override
	protected void onStartLoading()
	{	
		Log.d("CmiLoader.onStartLoading", "*****************************************************START ");
		
		if (this.cmiPage != null)
		{
			deliverResult(this.cmiPage);
			Log.d("CmiLoader.onStartLoading", dedication.toString() + " cmiPage still has valid data: " + cmiPage.toString().length());
			if (takeContentChanged())
				forceLoad();
		}
		else
		{
			Log.d("CmiLoader.onStartLoading", dedication.toString() + " cmiPage is null");
			//loadFromCache();
			
			// regardless whether or not cached data was available, also load fresh data now!
			forceLoad();
		}
		Log.d("CmiLoader.onStartLoading", "*****************************************************END ");
	}
	
	public String getDataId()
	{
		return dedication.toString() + userId + dateStart + machId;
	}
	
	
	@Override
	protected void onStopLoading() 
	{
		Log.d("CmiLoader.onStopLoading", dedication.toString() + " ");
	}
	
	@Override
	public void onCanceled(Document document)
	{
		Log.d("CmiLoader.onCanceled", dedication.toString() + " ");
	}
	 
	@Override
	protected void onReset() 
	{
		Log.d("CmiLoader.onReset", dedication.toString() + " Deleting cmiPage");
		
		//saveToCache();
		this.cancelLoad();
	}
	
	private void saveToCache()
	{
		if (this.cmiPage != null)
		{
			Context context = getContext();
			SharedPreferences preferences = context.getSharedPreferences("CMI_PAGE_CACHE", Context.MODE_PRIVATE);
			preferences.edit().putString(getDataId(), this.cmiPage.html()).apply();
			this.cmiPage = null;
		}
	}
	
	private void loadFromCache()
	{
		Context context = getContext();
		SharedPreferences preferences = context.getSharedPreferences("CMI_PAGE_CACHE", Context.MODE_PRIVATE);
		String html = preferences.getString(getDataId(), "");
		if (html.length() > 0)
		{
			// baseUri is a dummy - shouldn't cause any trouble... hopefully.
			this.cmiPage = new Document("http://cmisrv1.epfl.ch/reservation/");
			this.cmiPage.append(html);
			Log.d("CmiLoader.onStartLoading", "SUCCESSFULLY RELOADED CACHED DATA");
			deliverResult(this.cmiPage); // deliver cached data. 
		}
	}
	

	@Override
	public void deliverResult(Document document)
	{		
		if (document == null)
			Log.d("CmiLoader.deliverResult", dedication.toString() + " new Document: null");
		else
			Log.d("CmiLoader.deliverResult", dedication.toString() + " new Document:      " + document.toString().length());
		
		if (cmiPage == null)
			Log.d("CmiLoader.deliverResult", dedication.toString() + " previous Document: null");
		else
			Log.d("CmiLoader.deliverResult", dedication.toString() + " previous Document: " + cmiPage.toString().length());
		
		
		if (isReset()) 
		{
			Log.d("CmiLoader.deliverResult", dedication.toString() + "  Current State is RESET: deleting cmiPage");
			this.cmiPage = null;
			return;
	    }
 
	    // Hold a reference to the old data so it doesn't get garbage collected.
	    // We must protect it until the new data has been delivered.
	    Document oldPage = cmiPage;
	    this.cmiPage = document;
	 
	    if (isStarted()) 
	    {
	    	// If the Loader is in a started state, deliver the results to the
	    	// client. The superclass method does this for us.
	    	super.deliverResult(document);
	    	Log.d("CmiLoader.deliverResult", dedication.toString() + "  Current State is STARTED: delivering the document");
	    	
	    	if (document == null && oldPage != null)
	    	{
	    		String text = "Loading failed. This data may not be up to date.";
	    		Toast toast = Toast.makeText(this.getContext(), text, Toast.LENGTH_LONG);
				toast.show();
	    	}
	    	
	    	if (reloadInterval > 0)
				reloadTimer.schedule(new ReloadTask(), 1000l * reloadInterval);
	    }
	 
	    // Invalidate the old data as we don't need it any more.
	    if (oldPage != null && oldPage != document) 
	    	oldPage = null;
		
	}
	
	@Override
	public Document loadInBackground() 
	{
		Log.d("CmiLoader.loadInBackground", dedication.toString() + " ");
		if (username == null || password == null)
			return null;
		else
			return loadPage(dedication);
	}
	
	public Document loadPage(PageType page)
	{
		Document document = null; // empty document
		Connection connection;
		
		try 
		{
			switch (page)
			{
			case MAIN_PAGE:
				connection = Jsoup.connect(mainPageUrl);
				connection.data("login", username);
				connection.data("password", password);
				document = connection.post();
				// need to check if access was granted, i.e. credentials were correctly set
				break;
				
			case USER_LIST:
				connection = Jsoup.connect(userListUrl);
				connection.cookie("CMI_user", "1");
				document = connection.get();
				break;
				
			case DEBUG_USER_LIST:
				document = new Document(userListUrl);
				Context context = super.getContext();
				String html = context.getResources().getString(R.string.debug_user_page);
				document.append(html);
				break;
				
			case MAIN_PAGE_RES:
				connection = Jsoup.connect(mainPageUrl);
				connection.data("login", username);
				connection.data("password", password);
				connection.data("ID_Machine", machId);
			//	connection.data("mode", "preview");
				connection.data("mode", "reserve");
				connection.data("privilege", "simple");
				document = connection.post();
				break;
				
			case MAIN_PAGE_CONFIG_RES:
				connection = Jsoup.connect(mainPageUrl);
				connection.data("login", username);
				connection.data("password", password);
				connection.data("ID_Machine", machId);
				connection.data("mode", "calendar"); // triggers configuration view
				connection.data("privilege", "simple");
				
				for (Configuration.Setting setting : config)
					connection.data(setting.getId(), setting.getCurrent().value);
				
				document = connection.post();
				break;
				
			case ALL_RESERVATIONS_PAGE:
				connection = Jsoup.connect(allReservPageUrl);
				connection.data("list1[]", machId);
				connection.data("ResDate", dateStart);
				connection.data("ResDate2", dateEnd);
				connection.data("resbut", "Display");
				connection.data("order", "t3.DateRes,t2.NomMachine");
				document = connection.post();
				break;
				
			case USER_RESERVATIONS_PAGE:
				connection = Jsoup.connect(userReservPageUrl);
				connection.data("ID_User", userId);
				connection.data("order1", "t2.NomMachine");
				connection.data("order2", "t3.DateRes");
				document = connection.post();
				break;
				
			case NEWS_PAGE:
				connection = Jsoup.connect(newsPageUrl);
				connection.data("news_id", newsId);
				document = connection.post();
				document.attr("newsId", newsId);
				break;
				
			case NEWS_PAGE_CONCAT:
				document = newsLoad(newsCap);
				break;
				
			default:
				document = new Document("");
				
			}
		}
		catch (IOException ioException) 
		{
			lastException = ioException;
			Log.d("CmiLoader.loadPage", "IOException loading CMI page: " + ioException.getMessage());
		}
		
		return document;
	}
	
	public Document newsLoad(int maxItems)
	{
		Document mainPage = loadPage(PageType.MAIN_PAGE); // empty document
		if (mainPage == null) return null;
		
		Elements anchors = mainPage.select("a[rel]");
		
		Document itemPage = null;
		Document allItemsPage = null;
		
		int counter = 0;
		for(Element a : anchors)
		{
			this.newsId = a.attributes().get("rel");

			itemPage = loadPage(PageType.NEWS_PAGE);
			if (itemPage == null) continue;
			
			Element newsDiv = itemPage.getElementsByClass("newsDiv").first();
			newsDiv.attr("newsId", this.newsId);
			
			if (itemPage == null || ++counter > maxItems) break;
			
			if (allItemsPage == null)
				allItemsPage = itemPage;
			else
				allItemsPage.body().appendChild(newsDiv);
		}		
		
		return allItemsPage;
	}
	
	public Exception getLastException()
	{
		return lastException;
	}
}
