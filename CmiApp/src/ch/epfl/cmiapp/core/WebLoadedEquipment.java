package ch.epfl.cmiapp.core;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import android.util.Log;

public class WebLoadedEquipment extends Equipment
{
	public static WebLoadedEquipment create(Element element)
	{
		WebLoadedEquipment equipment = null;
		try
		{
			equipment = new WebLoadedEquipment(element);
		}
		catch (RuntimeException exception)
		{
			Log.d("WebLoadedEquipment.create", "Parsing Failed of html element : " + element.toString());
		}
		return equipment;
	}
	
	public WebLoadedEquipment(Element element)
	{
		machId = element.attr("Value");
		if(!parseString(element.text()))
			throw new RuntimeException("Cannot create WebLoadedEquipment - parsing failed.");
	}
	
	public boolean parseString(String string)
	{
		Pattern pattern = Pattern.compile("Z(\\d\\d)\\s*([^-]+)(\\s-.*)?");
		Matcher matcher = pattern.matcher(string);
		
		if (matcher.matches())
		{
			if (matcher.group(3) != null)
				this.supplement = matcher.group(3).substring(2);
			
			if (matcher.group(1) != null && matcher.group(2) != null)
			{
				this.name = matcher.group(2);
				this.zone = Integer.parseInt(matcher.group(1));
				return true;
			}
		}
		
		return false;
	}
}
