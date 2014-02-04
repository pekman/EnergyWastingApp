package fi.aalto.pekman.energywastingapp.components;

import fi.aalto.pekman.energywastingapp.R;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

public abstract class AbstractCamera extends Component {

	protected volatile Camera cam = null;
	protected Preview preview = null;

	@Override
	public boolean isSupported() {
		return context.getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	/** UI component that shows the preview image from camera */
	protected abstract class Preview extends SurfaceView implements SurfaceHolder.Callback {
		
		public Preview() {
			super(context);
			
			SurfaceHolder h = getHolder();
			h.addCallback(this);
			
			// this deprecated method needs to be called on older API levels
			if(Build.VERSION.SDK_INT < 11)
				h.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d("Camera", "Surface created");
			
			try {
				cam.startPreview();
			} catch (Exception e) {
				Log.e("Camera", "Error starting camera preview: " + e.getMessage(), e);
				onError();
				return;
			}
			
			onStart(holder);
		}
		
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d("Camera", "Surface destroyed");
		}
		
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Log.d("Camera", "Surface changed");
		}
		
		/** Called when the camera has been initialized. */
		protected void onStart(SurfaceHolder holder) {}
		
		/** Called when there was an error initializing the camera. */
		protected void onError() {}
	}

	protected Camera getCamera() {
		try {
			return Camera.open();
		} catch (Exception e) {
			Log.e("AbstractCamera.getCamera()", e.toString(), e);
			return null;
		}
	}

	protected void turnOnFlashlight() {
		Parameters p = cam.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		cam.setParameters(p);
	}

	protected void addPreview(Preview preview) {
		this.preview = preview;
		((ViewGroup) context.findViewById(R.id.MainFrameLayout)).addView(preview, 0);
	}

	@Override
	public void stop() {
		if (preview != null) {
			((ViewGroup) context.findViewById(R.id.MainFrameLayout)).removeView(preview);
			preview = null;
		}
		if (cam != null) {
			cam.stopPreview();
			cam.release();
			cam = null;
		}
	}

	@Override
	public void onPause() {
		if (running)
			stop();
	}

	@Override
	public void onResume() {
		if (running)
			start();
	}

}
