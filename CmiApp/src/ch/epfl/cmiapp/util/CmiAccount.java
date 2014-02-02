package ch.epfl.cmiapp.util;

import java.io.InputStream;

public class CmiAccount
{
	
	private String username;
	private String password;
	private String email;
	
	private String sciper;
	private String GasparId;
	private String GasparPassword;
	
	private static CmiAccount instance;
	
	public static void instance()
	{
		
	}	
	
	public class Builder
	{
		
	}
	
	public boolean hasGasparData()
	{
		return false;
		
	}
	
	public InputStream getCmiPage()
	{
		return null;
		
	}
}
