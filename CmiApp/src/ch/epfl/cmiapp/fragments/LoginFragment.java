package ch.epfl.cmiapp.fragments;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import android.support.v4.content.Loader;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ch.epfl.cmiapp.CmiLoader;
import ch.epfl.cmiapp.R;
import ch.epfl.cmiapp.CmiLoader.PageType;
import ch.epfl.cmiapp.R.id;
import ch.epfl.cmiapp.R.layout;

/*
 * http://stackoverflow.com/questions/10057859/android-theme-holo-dialog-changing-blue-lines-to-orange
 * 
 * 
 * 
*/
 


public class LoginFragment extends DialogFragment
	implements DialogInterface.OnClickListener, Loader.OnLoadCompleteListener<Document>
{
	Activity activity = null;
	
	private TextView usernameEdit = null;
	private TextView passwordEdit = null;
	private TextView feedbackText = null;
	
	private String username = "";
	private String password = "";
	private String userId   = "";
	private String userFullName = "";
	
	
	public interface LoginDialogCallbacks
	{
		public void onLoginSuccessful();
		public void onLoginFailed();
	}
	
	
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		activity = this.getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
	    // Get the layout inflater
	    LayoutInflater inflater = activity.getLayoutInflater();

	    View content = inflater.inflate(R.layout.login_dialog, null);
	    this.usernameEdit = (TextView) content.findViewById(R.id.usernameEdit);
	    this.passwordEdit = (TextView) content.findViewById(R.id.passwordEdit);
	    this.feedbackText = (TextView) content.findViewById(R.id.feedbackText);
	    
	    this.feedbackText.setVisibility(View.INVISIBLE);
	    
	    builder.setView(content);
	    builder.setPositiveButton("Login", this);
	    builder.setNegativeButton("Cancel", this);
	    builder.setTitle("Set CMi Credentials");
	    return builder.create();
	}

	public void onClick(DialogInterface dialog, int button)
	{
		switch(button)
		{
		case DialogInterface.BUTTON_POSITIVE:
			activity.setProgressBarVisibility(true);
			activity.setProgressBarIndeterminateVisibility(true);
			
			this.username = usernameEdit.getText().toString();
			this.password = passwordEdit.getText().toString();
			
			testLogin();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			this.getDialog().cancel();
		}
		
	}
	
	public void testLogin()
	{
		CmiLoader loader = new CmiLoader(activity, CmiLoader.PageType.MAIN_PAGE);
		loader.setCredentials(username, password);
		loader.registerListener(0, this);
		loader.startLoading();
	}

	public void onLoadComplete(Loader<Document> loader, Document document)
	{
		activity.setProgressBarVisibility(false);
		activity.setProgressBarIndeterminateVisibility(false);
		
		if (document != null)
		{
			
			Element formElement = document.select("form[name=resform]").first();
			Element tdElement   = formElement.select("td").first();
			
			String text = tdElement.ownText();
			if (text.contains("Welcome"))
			{	// login successful
				
				Element inputElement = document.select("input[name=ID_User]").first();
				userId = inputElement.attr("value");
				userFullName = text.substring(8);
				
				storeValues();
				sendFeedback(true);
			}
			else
			{
				this.feedbackText.setVisibility(View.VISIBLE);
			}
			
		}
	}
	
	private void storeValues()
	{
		SharedPreferences preferences = activity.getSharedPreferences("CMI_CREDENTIALS", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString("CMI_USERNAME", username);
		editor.putString("CMI_PASSWORD", password);
		editor.putString("CMI_USERID", userId);
		editor.putString("USER_FULLNAME", userFullName);
		editor.commit();
	}
	
	public void sendFeedback(boolean success)
	{
		try
		{
			 // Instantiate the NoticeDialogListener so we can send events to the host
			LoginDialogCallbacks callbacks = (LoginDialogCallbacks) activity;
			
			if (success)
				callbacks.onLoginSuccessful();
			else
				callbacks.onLoginFailed();
			
     	} 
		catch (ClassCastException e)
		{
			// The activity doesn't implement the interface, throw exception
	        throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
		}
	}
	
}
