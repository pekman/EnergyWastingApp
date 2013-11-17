package com.github.robinbj86.energywastingapp.components;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

public class Flashlight extends Component {

	@Override
	public String getName() { return "Flashlight"; }

	private Camera cam = null;

	@Override
	public boolean isSupported() {
		return context.getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
	}

	@Override
	public void start() {
		if (cam == null && isSupported())
			cam = Camera.open();
		
		if (cam != null) {
			Parameters p = cam.getParameters();
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			cam.setParameters(p);
			cam.startPreview();
		}
	}

	@Override
	public void stop() {
		if (cam != null) {
			cam.stopPreview();
			cam.release();
			cam = null;
		}
	}

	@Override
	public void onPause() {
		if (cam != null) {
			cam.release();
			cam = null;
			markTurnedOff();
		}
	}

	@Override
	public void onResume() {
		if (isSupported()) {
			cam = Camera.open();
		}
	}
}
