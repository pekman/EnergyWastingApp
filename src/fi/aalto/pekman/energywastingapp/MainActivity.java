package fi.aalto.pekman.energywastingapp;

import fi.aalto.pekman.energywastingapp.components.*;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	/**
	 * List of components in the order they will be displayed.
	 */
	private Component[] components;

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
			
			CompoundButton control = (CompoundButton)
					layout.findViewById(R.id.ComponentListItemSwitch);
			control.setChecked(false);
			
			// set either a separate label or Switch text to show component name
			TextView tv = (TextView) layout.findViewById(R.id.ComponentListItemName);
			if (tv == null)
				tv = control;
			tv.setText(component.getName());

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
			
			// if the component has a settings dialog, make settings button open it
			Button settingsButton = (Button)
					layout.findViewById(R.id.ComponentListSettingsButton);
			DialogFragment dialog = component.getSettingsDialog();
			if (dialog != null) {
				settingsButton.setOnClickListener(component);
			}
			else {
				layout.removeView(settingsButton);
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
