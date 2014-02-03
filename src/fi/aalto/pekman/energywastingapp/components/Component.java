package fi.aalto.pekman.energywastingapp.components;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Abstract class for components (GPS, wi-fi, camera, etc.)
 */
public abstract class Component
	implements OnCheckedChangeListener, OnSeekBarChangeListener, View.OnClickListener {
	
	/** Application context */
	public static ActionBarActivity context;
	
	/** GUI control associated with the component */
	public CompoundButton uiControl;
	
	/** GUI control that shows the current adjustment value */
	public TextView uiAdjustmentValueLabel;
	
	/** Indicates, whether the component is currently turned on */
	public boolean running = false;
	
	/** Name displayed in user interface */
	public abstract String getName();
	
	/**
	 * Returns false if the component cannot be used.
	 * (e.g. if the functionality is not supported)
	 */
	public boolean isSupported() { return true; }
	
	/** Returns true if energy use of the component can be adjusted */
	public boolean isAdjustable() { return false; }
	
	/** Returns minimum adjustment value */
	public int getAdjustmentMin() { return 1; }
	
	/** Returns maximum adjustment value */
	public int getAdjustmentMax() {
		throw new UnsupportedOperationException("getAdjustmentMax() not supported");
	}
	
	/** Returns initial adjustment value */
	public int getAdjustmentDefault() { return getAdjustmentMax(); }
	
	/** Returns adjustment increment/decrement step size */
	public int getAdjustmentStep() { return 1; }
	
	/** Callback called when the adjustment is changed */
	protected void onAdjustmentChange(int value) {}
	
	/** Returns the string displayed next to the adjustment seek bar */
	protected String getAdjustmentDisplayValue(int value) {
		return Math.round(value * getAdjustmentStep() * (100.0f / getAdjustmentMax())) + "%";
	}
	
	// OnSeekBarChangeListener events:
	@Override public void onStopTrackingTouch(SeekBar seekBar) {}
	@Override public void onStartTrackingTouch(SeekBar seekBar) {}
	
	/**
	 * This is used for attaching this object to a seek bar.
	 *
	 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int value = (progress / getAdjustmentStep()) + getAdjustmentMin();
		onAdjustmentChange(value);
		uiAdjustmentValueLabel.setText( getAdjustmentDisplayValue(value) );
	}
	
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
	
	/** Return settings dialog if the component has one */
	public DialogFragment getSettingsDialog() {
		return null;
	}
	
	/**
	 * This is used for attaching this object to a button.
	 *
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		getSettingsDialog().show(
				context.getSupportFragmentManager(),
				getName() + "DialogFragment");
	}
	
	/**
	 * Set the GUI control to off position.
	 * Does not need to be called in {@link #stop()}.
	 */
	public void markTurnedOff() {
		uiControl.setChecked(false);
		running = false;
	}
	
	public void markTurnedOn() {
		uiControl.setChecked(true);
		running = true;
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
