package ch.epfl.cmiapp.util;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

//http://epaul.github.io/jsch-documentation/simple.javadoc/com/jcraft/jsch/Session.html#setPortForwardingL(int,%20java.lang.String,%20int)
// ssh -L 36000:cmisrvm1.epfl.ch:80 tremplin.epfl.ch -l 167382

public class CmiSshTunnel
{
	public void establish() throws JSchException
	{
		String host = "tremplin.epfl.ch";
		String user = "167382";
		String password = "2branchGraphene";
		int port = 22;
		
		String tunnelRemoteHost = "cmisrvm1.epfl.ch";
		int tunnelLocalPort = 9080;
		int tunnelRemotePort = 80;
		
		JSch jsch = new JSch();
		LocalUserInfo userInfo = new LocalUserInfo();
		Session session = jsch.getSession(user, host, port);
		
		session.setPassword(password);
		session.setUserInfo(userInfo);
		session.connect();
		session.setPortForwardingL(tunnelLocalPort, tunnelRemoteHost, tunnelRemotePort);
		
		System.out.println("Connected");
		//http://www.beanizer.org/site/index.php/en/Articles/Java-ssh-tunneling-with-jsch.html
	}
	 
	class LocalUserInfo implements UserInfo
	{
		String passwd = "2branchGraphene";
		public String  getPassword()                    { return passwd; }
		public boolean promptYesNo(String str)          { return true; }
		public String  getPassphrase()                  { return null; }
		public boolean promptPassphrase(String message) { return true; }
		public boolean promptPassword(String message)   { return true; }
		public void showMessage(String message)         { }
	}
	
	public void activateTremplin() throws IOException
	{
		Connection conn = Jsoup.connect("https://tremplin.epfl.ch/ssh.cgi");
		Document doc = conn.get();
		
	}
	
	
}
