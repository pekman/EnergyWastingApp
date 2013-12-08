package com.github.robinbj86.energywastingapp.components;

import android.app.Activity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Abstract class for components (GPS, wi-fi, camera, etc.)
 */
public abstract class Component implements OnCheckedChangeListener {
	
	/** Application context */
	public static Activity context;
	
	/** GUI control associated with the component */
	public CompoundButton uiControl;
	
	/** Indicates, whether the component is currently turned on */
	protected boolean running = false;
	
	/** Name displayed in user interface */
	public abstract String getName();
	
	/**
	 * Returns false if the component cannot be used.
	 * (e.g. if the functionality is not supported)
	 */
	public boolean isSupported() { return true; }
	
	/**
	 * This is used for attaching this object to a toggle button.
	 *
	 * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged(android.widget.CompoundButton, boolean)
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			running = true;
			start();
		}
		else {
			stop();
			running = false;
		}
	}
	
	/**
	 * Set the GUI control to off position.
	 * Does not need to be called in {@link #stop()}.
	 */
	protected void markTurnedOff() {
		uiControl.setChecked(false);
		running = false;
	}
	
	protected void markTurnedOn() {
		uiControl.setChecked(true);
	}
	
	/** Called when the application is no longer in the foreground */
	public void onPause() {}
	/** Called when the application cones back into the foreground */
	public void onResume() {}
	
	/** Start the activity of the component */
	public abstract void start();
	/** Stop the activity of the component */
	public abstract void stop();
}
