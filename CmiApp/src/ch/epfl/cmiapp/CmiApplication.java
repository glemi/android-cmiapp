package ch.epfl.cmiapp;

import org.acra.ACRA;
import org.acra.annotation.*;
import android.app.Application;
import android.content.Context;

//@ReportsCrashes(
//    formKey = "",
//    // Your usual ACRA configuration
//    httpMethod = org.acra.sender.HttpSender.Method.PUT,
//    reportType = org.acra.sender.HttpSender.Type.JSON,
//    formUri = "http://ny.iriscouch.com/acra-cmi/_design/acra-storage/_update/report",
//    formUriBasicAuthLogin = "bugreporter",
//    formUriBasicAuthPassword = "password"
//)

//@ReportsCrashes(formKey = "dDJnQnZhaW1YRWZDR0VJeGpsQ3lDQnc6MQ") 
public class CmiApplication extends Application
{
	
	private static Context context;

	public static Context getAppContext() 
	{
        return context;
    }
	
	@Override
	public void onCreate()
	{
		ACRA.init(this);
		context = this.getApplicationContext();
		super.onCreate();
	}
}
