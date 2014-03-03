package ch.epfl.cmiapp.util;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class CmiHttpRequest
{
	protected HttpURLConnection connection;
	
	private String data = new String();
	private String cookies = new String();
	
	public CmiHttpRequest(HttpURLConnection connection)
	{
		this.connection = connection;
	}
	
	public void setData(String name, String value)
	{
		if (data.isEmpty())
			data = name + "=" + value;
		else
			data = data + "&" + name + "=" + value;
	}
	
	public void setCookie(String name, String value)
	{
		if (cookies.isEmpty())
			cookies = name + "=" + value;
		else
			cookies = cookies + ";" + name + "=" + value;
	}
	
	public void send() throws IOException
	{
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setFixedLengthStreamingMode(data.length());
		connection.setRequestProperty("Cookie", cookies);
		
		BufferedOutputStream stream;
		stream = new BufferedOutputStream(connection.getOutputStream());
		PrintWriter writer = new PrintWriter(stream);
		
		writer.write(data);
		writer.flush();
		writer.close();
	}
	
}
