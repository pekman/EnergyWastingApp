package com.github.robinbj86.energywastingapp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.robinbj86.energywastingapp.components.*;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
	 * List of components in the order they will be displayed.
	 */
	private Component[] components;

	/**
	 * Components that should be turned on for maximum power consumption.
	 * The items are indexes of components array.
	 */
	private static final Set<Integer> maxPowerComponents = new HashSet<Integer>(Arrays.asList(new Integer[] {
			0,1,2,3,4,  6,7,  9,10,11
		}));

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Component.context = this;

		// List of components in the order they will be displayed.
		// When adding new components, they should be instantiated here.
		Component[] components = {
				new CPUBurn(),
				new Display(),
				new WiFiDataTransfer(),
				new GPSCoordSearch(),
				new BlueToothBurn(),
				new AudioPlay(),
				new TonePlay(),
				new StillCamera(),
				new VideoCamera(),
				new RecordAudio(),
				new AppDirFileWriter(),
				new ExtStorageFileWriter()
		};
		this.components = components;

		for (Component component : components) {

			// create a switch control for the component
			Switch control = new Switch(this);
			control.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT ));
			control.setChecked(false);
			control.setText(component.getName());

			if (! component.isSupported()) {
				control.setEnabled(false);
				control.setClickable(false);
			}

			// add control to the UI
			((ViewGroup) findViewById(R.id.MainLinearLayout)).addView(control);

			// add the component object as listener for on/off toggle events
			control.setOnCheckedChangeListener(component);
			component.uiControl = control;
		}
	}

	public void turnAllOff(View view) {
		for (Component c : components) {
			if (c.running) {
				c.markTurnedOff();
			}
		}
	}

	private Toast toast;

	public void maxPowerConsumption(View view) {
		boolean anythingTurnedOn = false;
		
		// turn off components not in the list
		for (int i=0; i < components.length; i++) {
			if (components[i].running && ! maxPowerComponents.contains(i)) {
				components[i].markTurnedOff();
				anythingTurnedOn = true;
			}
		}
		
		toast = Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG);
		toast.show();
		
		// turn on components in the list (after 2s delay if anything was turned on)
		((ViewGroup) findViewById(R.id.MainLinearLayout)).postDelayed(new Runnable() {
				@Override
				public void run() {
					for (int i : maxPowerComponents) {
						if (! components[i].running && components[i].isSupported()) {
							components[i].markTurnedOn();
						}
					}
					toast.cancel();
				}
			},
			anythingTurnedOn ? 2000 : 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_TonePlay_settings:
			new ToneSettingsDialog().show(getFragmentManager(), "ToneSettingsDialogFragment");
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		for (Component c : components) {
			c.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		for (Component c : components) {
			c.onResume();
		}
	}

}
