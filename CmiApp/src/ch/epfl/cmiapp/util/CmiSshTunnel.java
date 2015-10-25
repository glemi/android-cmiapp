package ch.epfl.cmiapp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.util.Log;
import ch.epfl.cmiapp.CmiApplication;
import ch.epfl.cmiapp.R;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

// http://epaul.github.io/jsch-documentation/simple.javadoc/com/jcraft/jsch/Session.html#setPortForwardingL(int,%20java.lang.String,%20int)
// ssh -L 36000:cmisrvm1.epfl.ch:80 tremplin.epfl.ch -l 167382


// http://developer.android.com/training/articles/security-ssl.html#HttpsExample
// http://stackoverflow.com/questions/7744075/how-to-connect-via-https-using-jsoup
// http://developer.android.com/reference/java/net/HttpURLConnection.html
// http://tequila.epfl.ch/download.html
// http://tequila.epfl.ch/download/2.0/docs/writing-clients.pdf
// file:///home/glemi/Workspaces/Java/libraries/tequila-java-client-2.0.2/doc/javadoc/index.html

public class CmiSshTunnel
{

	private String sciper;
	
	private Session session;
	private CmiAccount account;
	
	public static final String tunnelRemoteHost = "cmisrvm1.epfl.ch";
	public static final int tunnelLocalPort = 9080;
	public static final int tunnelRemotePort = 80;
	
	public CmiSshTunnel(CmiAccount account)
	{
		this.account = account;
	}
	
	public boolean isActive()
	{
		return (session == null) ? false : session.isConnected(); 
	}
	
	public String getBaseUrl()
	{
		return "https://127.0.0.1:" + tunnelLocalPort;
	}
	
	public void establish() throws JSchException 
	{
		System.out.println("CmiSshTunnel.establish");
		if (isActive()) return;
		
		String host = "tremplin.epfl.ch";
		String user = account.getSciper();
		String pass = account.getGasparPassword();
		int port = 22;
		
		String tunnelRemoteHost = "cmisrvm1.epfl.ch";
		
		
		JSch jsch = new JSch();
		LocalUserInfo userInfo = new LocalUserInfo();
		
		System.out.println("Creating a session...");
		session = jsch.getSession(user, host, port);
		
		
		session.setPassword(pass);
		session.setUserInfo(userInfo);
		session.connect();
		
		try {
			session.setPortForwardingL(tunnelLocalPort, tunnelRemoteHost, tunnelRemotePort);
		}
		catch (JSchException exception)
		{
			if (!(exception.getCause() instanceof java.net.BindException))
				throw exception;
			System.out.println("Tunnel already connected.");
		}

		System.out.println("Connected");
		//http://www.beanizer.org/site/index.php/en/Articles/Java-ssh-tunneling-with-jsch.html
	}

	
	public void close() throws JSchException
	{
		if (session != null)
		{
			session.delPortForwardingL(tunnelLocalPort);
			session.disconnect();
		}
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
	
	@Override
	protected void finalize() throws Throwable
	{
		if (session != null)
		{
			close();;
			Log.w("CmiSshTunnel.finalize", "Tunnel was left open. Closing now.");
		}
		
		super.finalize();
	}
}


