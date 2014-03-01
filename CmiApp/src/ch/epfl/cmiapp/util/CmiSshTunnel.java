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
import ch.epfl.cmiapp.CmiApplication;
import ch.epfl.cmiapp.R;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

//http://epaul.github.io/jsch-documentation/simple.javadoc/com/jcraft/jsch/Session.html#setPortForwardingL(int,%20java.lang.String,%20int)
// ssh -L 36000:cmisrvm1.epfl.ch:80 tremplin.epfl.ch -l 167382


// http://developer.android.com/training/articles/security-ssl.html#HttpsExample
// http://stackoverflow.com/questions/7744075/how-to-connect-via-https-using-jsoup
// http://developer.android.com/reference/java/net/HttpURLConnection.html
// http://tequila.epfl.ch/download.html
// http://tequila.epfl.ch/download/2.0/docs/writing-clients.pdf
// file:///home/glemi/Workspaces/Java/libraries/tequila-java-client-2.0.2/doc/javadoc/index.html

public class CmiSshTunnel
{
	SSLContext sslContext;
	String sciper;
	
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
	
	public boolean activateTremplin() throws IOException, KeyManagementException, CertificateException, KeyStoreException, NoSuchAlgorithmException
	{
		// Implement detection of bad login. 
		HttpsURLConnection conn = gasparLogin("cnyffele", "2branchGraphene");
		return activateSshAccount(conn);
	}
	
	
	private HttpsURLConnection gasparLogin(String username, String password) throws KeyManagementException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException
	{
		String tremplinUrl = "https://tremplin.epfl.ch/ssh.cgi";
		HttpsURLConnection connection = setupConnection(tremplinUrl);
		connection.connect();
		
		Document doc = Jsoup.parse(connection.getInputStream(), null, tremplinUrl);
		Element element1 = doc.getElementById("requestkey");
		Element element2 = doc.getElementById("loginform");
		String address = element2.attr("action");
		String key     = element1.attr("value");
		connection.disconnect();
		
		HttpsURLConnection conn1 = setupConnection(address);
		//connection = s
		conn1.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn1.setRequestMethod("POST");
		conn1.connect();
		// in the response; check for the cookies "tequila_key" and "tequila_user"
		// defined using "Set-Cookie" header field
		// the key in tequila_key is different from the requestkey extracted above
		
		// content should look like this:
		// requestkey=erpmjprtujh1oixetmbqeikhspvdnxxb&username=cnyffele&password=2branchGraphene&login.x=44&login.y=21
		OutputStream outStream = conn1.getOutputStream();
		PrintWriter writer = new PrintWriter(outStream);
		writer.write("requestkey=");
		writer.write(key);
		writer.write("&username=");
		writer.write(username);
		writer.write("&password=");
		writer.write(password);
		writer.flush();
		
		return conn1;
	}
	
	private boolean activateSshAccount(HttpsURLConnection connection) throws KeyManagementException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException
	{
		Document doc = Jsoup.parse(connection.getInputStream(), null, connection.getURL().toString());
		
		System.out.println(doc.html());
		
		Element element1 = doc.select("input[name=key]").first();
		Element element2 = doc.select("input[name=hmac]").first();
		Element element3 = doc.select("input[type=submit]").first();
		String key = element1.val();
		String hmac = element2.val();
		String submit = element3.attr("name");
		
		if (submit.equals("delete")) // ssh already activated
			return true;
		
		connection.disconnect();
		
		HttpsURLConnection conn1;
		conn1 = setupConnection("https://tremplin.epfl.ch/ssh.cgi/admin");
		conn1.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn1.setRequestMethod("POST");
		conn1.setDoOutput(true);
		conn1.connect(); 
		
		OutputStream outStream = conn1.getOutputStream();
		PrintWriter writer = new PrintWriter(outStream);
		writer.write("create=Créer un compte");
		writer.write("&key=");
		writer.write(key);
		writer.write("&hmac=");
		writer.write(hmac);
		writer.flush();

		doc = Jsoup.parse(conn1.getInputStream(), null, conn1.getURL().toString());
		Element element4 = doc.select("input[type=submit]").first();
		Element element5 = doc.select("p strong").first();
		submit = element4.attr("name");
		sciper = element5.text();
		
		System.out.println(doc.html());
		
		return submit.equals("delete");
	}
	
	private HttpsURLConnection setupConnection(String address) throws KeyManagementException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException
	{
		if (sslContext == null) setupCertificates();
		
		URL url = new URL(address);
		HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
		urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
		return urlConnection;
	}
	
	
	private void setupCertificates() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException
	{
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		
		//loadCertificateResource(keyStore, R.raw.tequila, "tequila");
		loadCertificateResource(keyStore, R.raw.quovadis_root, "ca");
		loadCertificateResource(keyStore, R.raw.quovadis, "quovadis");

		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);

		// Create an SSLContext that uses our TrustManager
		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tmf.getTrustManagers(), null);
	}
	
	private void loadCertificateResource(KeyStore keyStore, int resId, String alias) throws CertificateException, IOException, KeyStoreException
	{
		Context context = CmiApplication.getAppContext();
		InputStream stream = context.getResources().openRawResource(resId);
		
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate certificate;
		try {
		    certificate = cf.generateCertificate(stream);
		    keyStore.setCertificateEntry(alias, certificate);
		    System.out.println(alias + "=" + ((X509Certificate) certificate).getSubjectDN());
		} finally {
			stream.close();
		}
	}
	
}


