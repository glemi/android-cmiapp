package ch.epfl.cmiapp.fragments;

import java.util.*;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.widget.LinearLayout.*;

import ch.epfl.cmiapp.core.CmiEquipment;
import ch.epfl.cmiapp.core.CmiEquipment.Configuration;
import ch.epfl.cmiapp.core.CmiEquipment.Configuration.Option;
import ch.epfl.cmiapp.core.CmiEquipment.Configuration.Setting;

public class ConfigDialogFragment extends DialogFragment
	implements DialogInterface.OnClickListener
{
	private CmiEquipment equipment;
	private Map<Setting, Spinner> map;
	
	private Callbacks listener;
	
	public interface Callbacks
	{
		public void onConfigChange(CmiEquipment.Configuration newConfig);
		public void onConfigCancel();
	}
	
	public void setListener(Callbacks listener)
	{
		this.listener = listener;
	}
	
	public void setEquipment(CmiEquipment equipment)
	{
		this.equipment = equipment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Context context = this.getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		View content = generateViewContent(context);
	    
	    builder.setView(content);
	    builder.setPositiveButton("OK", this);
	    builder.setNegativeButton("Cancel", this);
	    builder.setTitle("Edit Configuration");
	    return builder.create();
	}
	

	public View generateViewContent(Context context)
	{
		map = new HashMap<Setting, Spinner>();
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.RIGHT;
		layout.setLayoutParams(params);
		layout.setGravity(Gravity.RIGHT);
		
		for (Setting setting : equipment.config.settings)
		{
			if (setting.display == 0) continue;
			
			TextView textView = new TextView(context);
			textView.setText(setting.title);
			textView.setGravity(Gravity.CENTER_HORIZONTAL);
			layout.addView(textView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			
			Spinner spinner = new Spinner(context);
			final int layoutId = android.R.layout.simple_spinner_dropdown_item;
			ArrayAdapter<Option> adapter = new ArrayAdapter<Option>(context, layoutId, setting.options); 
			spinner.setAdapter(adapter);
			
			int position = adapter.getPosition(setting.getCurrent());
			spinner.setSelection(position);
			spinner.setGravity(Gravity.RIGHT);
			layout.addView(spinner, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			map.put(setting, spinner);
		}
		
		return layout;
	}
	
	
	public void readSelection()
	{
		for (Setting setting : equipment.config.settings)
		{
			// echt schöner code! 
			Spinner spinner = map.get(setting);
			if (spinner == null) continue;
			int optionIndex = spinner.getSelectedItemPosition();
			Option selected = setting.options.get(optionIndex);
			setting.currentValue = selected.value;
		}
	}
	
	public void onClick(DialogInterface dialog, int button)
	{
		switch(button)
		{
		case DialogInterface.BUTTON_POSITIVE:
			readSelection();
			listener.onConfigChange(equipment.config);
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			this.getDialog().cancel();
			listener.onConfigCancel();
		}
	}	
}
