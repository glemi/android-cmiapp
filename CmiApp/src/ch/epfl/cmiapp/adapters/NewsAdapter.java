package ch.epfl.cmiapp.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ch.epfl.cmiapp.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author glemi
 *
 */
public class NewsAdapter extends CmiPageAdapter
{
	private SortedSet<CmiNewsItem> newsItems = new TreeSet<CmiNewsItem>();
	private List<CmiNewsItem> newsList = new ArrayList<CmiNewsItem>();

	public class CmiNewsItem
		implements java.lang.Comparable<CmiNewsItem>
	{
		public static final long CMI_DATA_TYPE_ID = 2l;
		
		public int newsId;
		public LocalDate date;
		public String title;
		public String content;
		
		public int compareTo(CmiNewsItem other)
		{
			if (this.date.compareTo(other.date) == 0)
				return this.newsId - other.newsId;
			else
				return -this.date.compareTo(other.date);
		}
		
		public long getId()
		{
			return CmiNewsItem.CMI_DATA_TYPE_ID | newsId << 8;	
		}
	}
	
	public NewsAdapter()
	{
		this.setSectionsEnabled(false);
	}
	
	@Override
	protected void onParseData(Document page)
	{
		if(page == null) return;
		
		Elements newsDivs = page.getElementsByClass("newsDiv");
		
		for (Element newsDiv : newsDivs)
		{
			Element header1Div 	= newsDiv.select("div[class=newsHeader]").get(0);
			Element header2Div 	= newsDiv.select("div[class=newsHeader]").get(1);
			Element contentDiv 	= newsDiv.select("div[class=content]").first(); 
			
			String idString     = newsDiv.attr("newsId");
			String dateString 	= header1Div.select("em").first().text();
			String title	  	= header2Div.select("strong").first().text();
			String allContent 	= contentDiv.html();
			String content 	  	= "";
			
			DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/mm/yy");
			LocalDate date = formatter.parseLocalDate(dateString.trim());
			
			String[] lines = allContent.split("<br\\s?/>");
			
			for (int k = 0; k < lines.length; k++)
				if (lines[k].length() > content.length())
					content = lines[k];
			
			CmiNewsItem newsItem = new CmiNewsItem();
			newsItem.newsId = Integer.parseInt(idString);
			newsItem.date = date;
			newsItem.title = title;
			newsItem.content = content;
			Log.d("NewsAdapter.onParseData", newsItem.content.substring(0, 25) + "...");
			newsItems.add(newsItem);
		}	
		
		newsList.addAll(newsItems);
		
		Log.d("NewsAdapter.onParseData", "Parsing News Data: " + newsItems.size() + " items.");
		
		notifyDataSetChanged();
	}
	
	/*
	 * 
	 * <body>
	 *	<div class="newsDiv">
	 *	  <div class="newsHeader">Posted on: <em>04/01/2013</em>, By <em>PHILIPPE FLUCKIGER</em></div>
	 *	  <div class="newsHeader"><strong>New Stress Measurement System</strong></div>
	 *	  <div class="content">A new Thin Film Stress Measurement System FLX 2320-S from Toho Technology Inc. is available in CMi.<br />
	 *	<br />
	 *	Contact Person: Philippe Langlet<br />
	 *	<br />
	 *	With my very best regards,<br />
	 *	Philippe</div>
	 *	</div>
	 *	<br />
	 *	</body>
	 *
	 **/
	
	@Override
	public String getEmptyText()
	{
		return "No News";
	}

	@Override
	public View getItemView(int position, View convertView, ViewGroup parent)
	{
		Context context 		= parent.getContext();
		Resources res 			= context.getResources();
		Object service  		= context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LayoutInflater inflater = (LayoutInflater) service;	

		View row = convertView;
		//if (row == null)
			row = inflater.inflate(R.layout.news_item, parent, false);
			
		if (convertView != null && convertView.getId() != R.layout.news_item)
		{
			int viewtype = this.getItemViewType(position);
			Log.d("NewsAdapter.getItemView", "wrong type convertView (news item); viewtype is " + viewtype);
			
		}

		TextView dateView = (TextView) row.findViewById(R.id.date);
		TextView titleView = (TextView) row.findViewById(R.id.title);
		TextView contentView = (TextView) row.findViewById(R.id.content);
		
		CmiNewsItem item = newsList.get(position);
		dateView.setText(item.date.toString("dd-MM-yy"));
		titleView.setText(item.title);
		contentView.setText(item.content);

		Log.d("NewsAdapter.getItemView", "\t\t\t\tView Text = " + item.content);
		
		row.setTag(item);
		return row;
	}
	

	@Override
	public int getItemCount()
	{
		// No headers here! The grey area is part of each news list item. 
		// Log.d("NewsAdapter.getItemCount", "News Item Count: " + newsItems.size());
		return newsItems.size();
	}
	
	@Override
	public long getItemId(int position)
	{
		//Log.d("NewsAdapter.getItemId", "ItemId(" + position + ") = " + newsList.get(position).getId());
		return newsList.get(position).getId();
	}
	
	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public String getSection(int position)
	{
		return null;
	}
	
	@Override
	public boolean isEnabled(int position)
	{
		return false;
	}
	
}
