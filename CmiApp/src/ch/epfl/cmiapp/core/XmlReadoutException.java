package ch.epfl.cmiapp.core;

public class XmlReadoutException extends Exception
{
	public XmlReadoutException(String message) 
	{
		super("XML Readout Error: " + message); 
	}
}
