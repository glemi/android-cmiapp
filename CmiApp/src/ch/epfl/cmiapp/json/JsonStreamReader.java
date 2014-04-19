package ch.epfl.cmiapp.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonStreamReader extends BufferedReader
{
	public JsonStreamReader(InputStream in)
	{
		super(new InputStreamReader(in));
	}
	
	
	public JSONObject readJason() throws JSONException, IOException
	{
		StringBuilder builder = new StringBuilder();
		String line = null;
	    
		while ((line = readLine()) != null)
	    	builder.append(line);
	    
	    return new JSONObject(builder.toString());
	}
	
}
