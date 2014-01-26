package ch.epfl.cmiapp.fragments;

import java.util.*;

import android.app.Activity;
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

import ch.epfl.cmiapp.core.Equipment;
import ch.epfl.cmiapp.core.Configuration;
import ch.epfl.cmiapp.core.Configuration.Option;
import ch.epfl.cmiapp.core.Configuration.Setting;
import ch.epfl.cmiapp.util.EquipmentManager;

public class ConfigDialogFragment extends DialogFragment
	implements DialogInterface.OnClickListener
{
	private Equipment equipment;
	private Map<Setting, Spinner> map;
	
	private Callbacks listener;
	
	public interface Callbacks
	{
		public void onConfigChange(Configuration newConfig);
		public void onConfigCancel();
	}
	
	public void setListener(Callbacks listener)
	{
		this.listener = listener;
	}
	
	public void setEquipment(Equipment equipment)
	{
		this.equipment = equipment;
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		listener = (Callbacks) activity;
		super.onAttach(activity);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Context context = this.getActivity();
		
		if (savedInstanceState != null)
			restoreInstanceState(savedInstanceState);
		
		View content = generateViewContent(context);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
		
		for (Setting setting : equipment.getConfig())
		{
			if (!setting.getsDisplayed()) continue;
			
			TextView textView = new TextView(context);
			textView.setText(setting.getTitle());
			textView.setGravity(Gravity.CENTER_HORIZONTAL);
			layout.addView(textView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			
			Spinner spinner = new Spinner(context);
			final int layoutId = android.R.layout.simple_spinner_dropdown_item;
			ArrayAdapter<Option> adapter = new ArrayAdapter<Option>(context, layoutId, setting.getOptions()); 
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
		for (Setting setting : equipment.getConfig())
		{
			// echt schöner code! 
			Spinner spinner = map.get(setting);
			if (spinner == null) continue;
			int optionIndex = spinner.getSelectedItemPosition();
			Option selected = setting.getOptions().get(optionIndex);
			setting.change(selected);
		}
	}
	
	public void onClick(DialogInterface dialog, int button)
	{
		switch(button)
		{
		case DialogInterface.BUTTON_POSITIVE:
			readSelection();
			listener.onConfigChange(equipment.getConfig());
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			this.getDialog().cancel();
			listener.onConfigCancel();
		}
	}	
	
	@Override
	public void onSaveInstanceState(Bundle instanceState)
	{
		instanceState.putString("machId", equipment.getMachId());

		int index = 0; 
		String[] settingIds = new String[map.keySet().size()];
		for(Setting setting : map.keySet() )
		{
			instanceState.putString(setting.getId(), setting.getValue());
			settingIds[index++] = setting.getId();
		}
		
		instanceState.putStringArray("settingIds", settingIds);
		
		super.onSaveInstanceState(instanceState);
	}
	
	// Doesn't override anything! gets called by onCreateDialog
	public void restoreInstanceState(Bundle instanceState)
	{
		String machId = instanceState.getString("machId");
		equipment = EquipmentManager.getInventory().get(machId);
		Configuration config = equipment.getConfig();
		
		String[] settingIds = instanceState.getStringArray("settingIds");
		for (String id : settingIds)
		{
			Setting setting = config.getSetting(id);
			String value = instanceState.getString(id);
			setting.change(value);
		}
	}
	
	
}
