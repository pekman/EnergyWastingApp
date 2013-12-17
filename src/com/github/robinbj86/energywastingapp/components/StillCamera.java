package com.github.robinbj86.energywastingapp.components;

import java.io.IOException;

import com.github.robinbj86.energywastingapp.R;

import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.VideoView;

public class StillCamera extends Component {

	@Override
	public String getName() { return "Camera (still photos)"; }

	private volatile Camera cam = null;
	private Preview preview = null;
	private volatile boolean takingPicture = false;
	private Thread thread;

	@Override
	public boolean isSupported() {
		return context.getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	/** UI component that shows the preview image from camera */
	private class Preview extends SurfaceView implements SurfaceHolder.Callback {
		
		public Preview() {
			super(context);
			getHolder().addCallback(this);
		}
		
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d("StillCamera", "Surface created");
			try {
				cam.setPreviewDisplay(holder);
				cam.startPreview();
				
				// start taking pictures
				thread.start();
			} catch (IOException e) {
				Log.d("StillCamera", "Error setting camera preview: " + e.getMessage());
			}
		}
		
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d("StillCamera", "Surface destroyed");
		}
		
		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Log.d("StillCamera", "Surface changed");
		}
	}

	private Camera getCamera() {
		try {
			return Camera.open();
		} catch (Exception e) {
			Log.e("StillCamera.getCamera()", e.toString(), e);
			return null;
		}
	}

	@Override
	public void start() {
		if (cam == null && isSupported())
			cam = getCamera();
		
		if (cam != null) {
			// create thread that takes pictures, but don't start it yet
			thread = new Thread() {
				@Override
				public synchronized void run() {
					takePicture();
					
					while (cam != null) {
						while (takingPicture) {
							try {
								wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (cam == null)
							return;
						
						cam.startPreview();
						takePicture();
					}
				};
			};
			
			// create and add preview UI component
			preview = new Preview();
			((ViewGroup) context.findViewById(R.id.MainFrameLayout)).addView(preview, 0);
		}
	}
	
	private void takePicture() {
		if (cam != null) {
			takingPicture = true;
			
			Log.d("StillCamera.takePicture()", "Taking picture");
			cam.takePicture(null, null, null, new Camera.PictureCallback() {
				@Override
				public void onPictureTaken(byte[] data, Camera camera) {
					Log.d("StillCamera.takePicture()", "Picture taken");
					
					// wake up picture taking thread so that it can take a new picture
					synchronized (thread) {
						takingPicture = false;
						thread.notify();
					}
				}
			});
		}
	}

	@Override
	public void stop() {
		if (cam != null) {
			cam.stopPreview();
			cam.release();
			cam = null;
		}
		if (preview != null) {
			((ViewGroup) context.findViewById(R.id.MainFrameLayout)).removeView(preview);
			preview = null;
		}
		if (thread != null) {
			// wake up picture taking thread so that it can stop properly
			synchronized (thread) {
				thread.notify();
			}
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
