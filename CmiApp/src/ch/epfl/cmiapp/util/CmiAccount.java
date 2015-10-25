package ch.epfl.cmiapp.util;

import java.io.InputStream;

import com.jcraft.jsch.JSchException;

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
	
	private TremplinAccountManager tremplin;
	private CmiSshTunnel tunnel = new CmiSshTunnel(this);
	private CmiServerConnection server;
	
	private static CmiAccount instance = null;
	
	public static CmiAccount instance()
	{
		return instance;
	}	
	
	public boolean hasGasparData()
	{
		return !gasparId.isEmpty() && !gasparPassword.isEmpty();
	}
	
	public boolean setupSshTunnel() throws JSchException 
	{
		tunnel.establish();
		return true;
/*		try {
			tunnel.establish();
			return true;
		}
		catch (JSchException exception)
		{
			return false;
		}*/
	}

	public CmiServerConnection getServerConnection()
	{
		if (server == null)
		{
			server = new CmiServerConnection(this);
		}
		
		return server;
	}
	
	public void closeSshTunnel() throws JSchException
	{
		tunnel.close();
	}
	
	public boolean isSshTunnelConnected()
	{
		return tunnel.isActive();
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
	public void setGasparId(String gasparId) { this.gasparId = gasparId; } 
	public void setGasparPassword(String gasparPassword) { this.gasparPassword = gasparPassword; }
}
