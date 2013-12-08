package com.github.robinbj86.energywastingapp.components;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

public class StillCamera extends AbstractCamera {

	@Override
	public String getName() { return "Camera (still photos) & flash"; }

	private volatile boolean takingPicture = false;
	private Thread thread;

	/** UI component that shows the preview image from camera */
	private class Preview extends AbstractCamera.Preview {
		
		protected void onStart(SurfaceHolder holder) {
			// start taking pictures
			thread.start();
		}
		
		protected void onError() {
			stop();
			markTurnedOff();
		}
	}

	@Override
	public void start() {
		if (cam == null && isSupported())
			cam = getCamera();
		
		if (cam != null) {
			turnOnFlashlight();
			
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
			addPreview(new Preview());
		}
		else
			markTurnedOff();
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
		super.stop();
		
		if (thread != null) {
			// wake up picture taking thread so that it can stop properly
			synchronized (thread) {
				thread.notify();
			}
		}
	}

}
