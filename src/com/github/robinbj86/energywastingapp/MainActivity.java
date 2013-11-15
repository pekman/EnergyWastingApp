package com.github.robinbj86.energywastingapp;

import com.github.robinbj86.energywastingapp.components.*;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Switch;

public class MainActivity extends Activity {

	/**
	 * List of components in the order they will be displayed.
	 * 
	 * When adding new components, they should be instantiated here.
	 */
	private Component[] components = {
			new CPUBurn(),
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		for (Component component : components) {

			// create a switch control for the component
			Switch control = new Switch(this);
			control.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT ));
			control.setChecked(false);
			control.setText(component.getName());

			// add control to the UI
			((ViewGroup) findViewById(R.id.MainLinearLayout)).addView(control);

			// add the component object as listener for on/off toggle events
			control.setOnCheckedChangeListener(component);
			component.context = this;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
