package ch.epfl.cmiapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CmiUser 
{
	public String 	firstName;
	public String 	lastName;
	public String   company;
	public String 	zoneString;
	public int 		zone;
	public String 	sinceTime;
	
	public String fullName()
	{
		if (company.length() > 0)
			return String.format("%s, %s - %s", lastName, firstName, company);
		else
			return String.format("%s, %s", lastName, firstName);
	}
	
	
	public CmiUser()
	{
		firstName = "";
		lastName  = "";
		company   = "";
		zone      = 0;
		sinceTime = "unknown";
	}
	
	public CmiUser(String firstName, String lastName, int zone, String since)
	{
		this.firstName = firstName;
		this.lastName  = lastName;
		this.zone      = zone;
		this.sinceTime = since;
	}
	
	public int hashCode()
	{
		String synthesis = firstName + lastName + company;
		return synthesis.hashCode();
	}
	
	public String toString()
	{
		return lastName.toUpperCase() + " " + firstName + " (" + zoneString + ")";
	}

}
