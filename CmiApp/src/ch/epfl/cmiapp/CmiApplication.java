package ch.epfl.cmiapp;

import org.acra.ACRA;
import org.acra.annotation.*;
import android.app.Application;

@ReportsCrashes(formKey = "dDJnQnZhaW1YRWZDR0VJeGpsQ3lDQnc6MQ") 
public class CmiApplication extends Application
{

	@Override
	public void onCreate()
	{
		ACRA.init(this);
		super.onCreate();
	}
	
}
