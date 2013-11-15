package com.github.robinbj86.energywastingapp.components;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Abstract class for components (GPS, wi-fi, camera, etc.)
 */
public abstract class Component implements OnCheckedChangeListener {
	
	/** Application context */
	public Context context;
	
	/** Name displayed in user interface */
	public abstract String getName();
	
	/**
	 * This is used for attaching this object to a toggle button.
	 *
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked)
			start();
		else
			stop();
	}
	
	public abstract void start();
	public abstract void stop();
}
