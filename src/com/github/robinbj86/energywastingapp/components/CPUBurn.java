package com.github.robinbj86.energywastingapp.components;

import android.widget.Toast;

public class CPUBurn extends Component {

	public String getName() { return "CPUburn"; }

	public CPUBurn() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void start() {
		Runtime.getRuntime().availableProcessors();
		Toast.makeText(context, "CPUburn start", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void stop() {
		Toast.makeText(context, "CPUburn stop", Toast.LENGTH_SHORT).show();
	}
	
}
