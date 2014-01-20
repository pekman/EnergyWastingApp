package fi.aalto.pekman.energywastingapp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fi.aalto.pekman.energywastingapp.R;
import fi.aalto.pekman.energywastingapp.components.*;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
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
				new ExtStorageFileWriter(),
				new Vibration()
		};
		this.components = components;
		
		ViewGroup list = (ViewGroup) findViewById(R.id.MainLinearLayout);
		
		for (Component component : components) {
			
			// add switch and adjustment controls for the component
			ViewGroup layout = (ViewGroup)
					getLayoutInflater().inflate(R.layout.component_list_item, list, false);
			
			Switch control = (Switch) layout.findViewById(R.id.ComponentListItemSwitch);
			control.setChecked(false);
			control.setText(component.getName());

			if (! component.isSupported()) {
				control.setEnabled(false);
				control.setClickable(false);
			}
			
			// add the component as listener for switch on/off events
			control.setOnCheckedChangeListener(component);
			component.uiControl = control;
			
			// if the component is adjustable, init adjustment control
			ViewGroup adjustmentLayout = (ViewGroup)
					layout.findViewById(R.id.ComponentListItemAdjustment);
			if (component.isAdjustable()) {
				int min = component.getAdjustmentMin();
				int max = component.getAdjustmentMax();
				int step = component.getAdjustmentStep();
				SeekBar adj = (SeekBar)
						layout.findViewById(R.id.ComponentListItemAdjustmentSeekBar);
				TextView label = (TextView)
						layout.findViewById(R.id.ComponentListItemAdjustmentLabel);
				
				label.setWidth(
						label.getPaddingLeft() + label.getPaddingRight() +
						(int) Math.ceil( label.getPaint().measureText("100%") ));
				component.uiAdjustmentValueLabel = label;
				
				// add the component as listener for adjustment change events
				adj.setMax((max-min)/step);
				adj.setOnSeekBarChangeListener(component);
				adj.setProgress(component.getAdjustmentDefault());
			}
			else {
				layout.removeView(adjustmentLayout);
			}
			
			// add controls to the UI
			list.addView(layout);
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
