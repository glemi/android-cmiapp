package ch.epfl.cmiapp.util;

import java.io.InputStream;

public class CmiAccount
{
	private String username;
	private String password;
	private String userid;
	
	private String fullName;
	private String email;
	
	private String sciper;
	private String gasparId;
	private String gasparPassword;
	
	private CmiSshTunnel tunnel;
	
	private static CmiAccount instance = null;
	
	public static CmiAccount instance()
	{
		return instance;
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
	
	public boolean setupSshTunnel()
	{

		return false;
	}
	
	
	
	
	public String getUsername() { return username; }
	public String getPassword() { return password; }
	public String getUserId()  { return userid; }
	public String getEmail() { return email; } 
	public String getSciper() { return sciper; } 
	public String getGasparId() { return gasparId; }
	public String getGasparPassword() { return gasparPassword; } 
	
	public void setUsername(String username) { this.username = username; }
	public void setPassword(String password) { this.password = password; }
	public void setUserid(String userid) { this.userid = userid; }
	public void setEmail(String email) { this.email = email; }
	public void setSciper(String sciper) { this.sciper = sciper; }
	public void setGasparId(String gasparId) { gasparId = gasparId; } 
	public void setGasparPassword(String gasparPassword) { gasparPassword = gasparPassword; }
}
