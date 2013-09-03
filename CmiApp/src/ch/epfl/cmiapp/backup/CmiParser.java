package ch.epfl.cmiapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.*;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import android.util.Log;


// http://java-source.net/open-source/html-parsers
// http://developer.android.com/reference/android/text/Html.html



// get machine usage status 
// http://cmisrv1.epfl.ch/eae/EAE_user.php
// parameters: Zone[]=1&Zone[]=2&Zone[]=3    ...etc.

// more info on machine
// http://cmisrv1.epfl.ch/eae/EAE_user_info.php
// parameters: ID_Machine=mach022

// very powerful: http://cmisrv1.epfl.ch/reservation/allreserv.php
// parameters: see image in worksapce folder

public class CmiParser 
{
	private String username;
	private String password;
	
	public void setCredentials(String username, String password)
	{
		this.username = username;
		this.password = password;
	}
	
	public boolean logon() throws IOException
	{
		String url = "http://cmisrv1.epfl.ch/reservation/reserv.php";
		url += "?login=" + username + "&password=" + password;
		Document doc;
		
		doc = Jsoup.connect(url).get();
		
		String alltext = doc.text();
		return alltext.contains("Welcome");
	}
	
	/*
	<div style = "text-align: left; margin-left: 5px;">1 users in the cleanroom</div>
	<div style = "display: inline-table; width: 315px; margin-left: 5px;">
		<table class = "cmi_recette" align = "center" cellpadding = "3" width = "310">
	    	<tr><th>Name</th><th width = "55">Zone</th><th width = "40">Since</th></tr>
	    	<<tr><td>SPINA Massimo</td><td>Zone 07</td><td>0:32</td></tr>
	    </table>
    </div>
    <div style = "text-align: right;">Table updated at 1:07</div>
	*/
	
	
	public List<CmiUser> getUsers() throws IOException
	{
		String url = "http://cmisrv1.epfl.ch/spc/utilSB/include/tabUtilLimit.php?hauteur=800";
		Connection connection = Jsoup.connect(url);
		connection.cookie("droit", "0"); // pas necessaire?
		connection.cookie("CMI_user", "1");
		
		List<CmiUser> users = new ArrayList<CmiUser>();
		Document doc  = connection.get();
		
		Elements elements = doc.select("td");
		
		if(elements.size() >= 3)
		{
			Iterator<Element> iter = elements.iterator();
			while (iter.hasNext()) 
			{
				CmiUser user = new CmiUser();
				
				Log.d("GetUsers()", iter.toString());
				
				String nameString = iter.next().text();
				String zoneString = iter.next().text();
				String timeString = iter.next().text();
				
				//String pattern = "(?<=\\p{IsLu}+)\\s(?=\\p{IsLu}\\p{IsLl}+)";
				String pattern = "(?:[A-Z]+)\\s(?=[A-Z][a-z]+)";
			    String[] nameParts = nameString.split(pattern, 2);
			    
			    if (nameParts.length == 2)
			    {
			    	user.firstName = nameParts[1];
			    	user.lastName  = nameParts[0]; 
			    }
			    else
			    {
			    	user.lastName  = nameString;
			    }
				
				user.sinceTime = timeString;
			    user.zone = 1;// Integer.parseInt(zoneString); 
			    
			    users.add(user);
			}
		}
		
		return users;
	}
	
	/*
	 * 	<td rowspan="4"><select name="ID_Machine" onchange="document.resform.mode.value='preview';document.resform.submit();" size="8" style="width:300px;font-size:12px"><option  Value="mach116" >Z01 Heidelberg DWL200 - Laser lithography system<br>
	 *	<option  Value="mach144" >Z01 Krypton Light Source Extension for DWL200<br>
	 *	<option  Value="mach003" >Z01 Suess DV10 - Developer for mask and thick positive resist<br>
	 *	<option  Value="mach048" >Z01 Zeiss LEO 1550 - Scaning Electron Microscope<br>
	 *	<option  Value="mach135" >Z02 Alcatel AMS200 DSE - Plasma etcher - Fluorine chemistry<br>
	 *	<option  Value="mach163" >Z02 Tepla GigaBatch - Microwave plasma stripper<br>
	 *	<option  Value="mach141" >Z03 Sopra GES 5E - Spectroscopic Ellipsometer<br>
	 *	<option  Value="mach155" >Z04 Beneq TFS200 - ALD<br>
	 *	<option  Value="mach136" >Z04 Leybold-Optics LAB600 H - Evaporator Lift-off<br>
	 *	<option  Value="mach028" >Z05 Tepla 300 - Microwave plasma stripper<br>
	 *	<option  Value="mach034" >Z06 Coillard Etching- Wet bench for oxide and metal etch<br>
	 *	<option  Value="mach033" >Z06 Coillard Photo - Wet bench for resist develop and strip<br>
	 *	<option  Value="mach030" >Z06 Suess RC8 THP - Manual Coater (entrance zone)<br>
	 *	<option  Value="mach039" >Z06 Suess RC8 THP - Manual Coater (mid-zone)<br>
	 *	<option  Value="mach140" >Z07 Vistec EPBG5000ES - Ebeam<br>
	 *	<option  Value="mach160" >Z13 Ceram Hotplate for high Temperature<br>
	 *	<option  Value="mach161" >Z13 SussMicroTec MJB4- Single side mask aligner<br>
	 *	<option  Value="mach157" >Z14 Wet Bench Acid<br>
	 *	<option  Value="mach165" >Z15 Zeiss Merlin - Scaning Electron Microscope<br>
		</select></td>
	 */
	
	
	
	
	public List<CmiEquipment> getEquipment() throws IOException
	{
		String url = "http://cmisrv1.epfl.ch/reservation/reserv.php?login=%s&password=%s";
		url = String.format(url, username, password);
		Connection connection = Jsoup.connect(url);
		
		List<CmiEquipment> eqptList = new ArrayList<CmiEquipment>();
		Document doc  = connection.get();
		
		Elements elements = doc.select("option[Value]");
		
		
		for(Element element : elements)
		{
			CmiEquipment equipment = new CmiEquipment();

		    Pattern pattern = Pattern.compile("Z(\\d\\d)\\s*([\\w\\s]+)\\s*(?:-\\s([\\w\\s]+))?");
		    Matcher matcher = pattern.matcher(element.text());
		    
		    if (matcher.matches())
		    {
		    	equipment.machId = element.attr("Value");
		    	
		    	equipment.supplement = matcher.group(3);
		    	equipment.name = matcher.group(2);
		    	equipment.zone = Integer.parseInt(matcher.group(1));
		    	eqptList.add(equipment);
		    	
		    	Log.d("CmiParser", equipment.name);
		    }
			
		}
		
		return eqptList;
	}
	
	
	/*
	public directorySearch(String name)
	{
		name = java.net.URLEncoder.encode(name, "UTF-8");
		String url = "http://m.epfl.ch/public/directory/search.do?q=";
		url += name;
		
	}
	*/
	
}
