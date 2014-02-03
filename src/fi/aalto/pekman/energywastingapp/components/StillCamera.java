package fi.aalto.pekman.energywastingapp.components;

import fi.aalto.pekman.energywastingapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RadioGroup;

public class StillCamera extends AbstractCamera {

	@Override
	public String getName() { return "Camera (still photos) & flash"; }

	private static String flashMode = Parameters.FLASH_MODE_TORCH;

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
			Parameters p = cam.getParameters();
			p.setFlashMode(flashMode);
			cam.setParameters(p);
			
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

	@Override
	public DialogFragment getSettingsDialog() {
		return new SettingsDialog();
	}

	public static class SettingsDialog extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_stillcamera_settings, null);
			
			int id;
			if (flashMode == Parameters.FLASH_MODE_TORCH)
				id = R.id.flashModeTorch;
			else if (flashMode == Parameters.FLASH_MODE_ON)
				id = R.id.flashModeOn;
			else if (flashMode == Parameters.FLASH_MODE_OFF)
				id = R.id.flashModeOff;
			else if (flashMode == Parameters.FLASH_MODE_RED_EYE)
				id = R.id.flashModeRedEye;
			else
				id = -1;
			((RadioGroup) view.findViewById(R.id.flashModeRadioGroup)).check(id);
			
			builder.setTitle("Camera & flash settings")
				.setView(view)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						RadioGroup wf = (RadioGroup)
								((Dialog) dialog).findViewById(R.id.flashModeRadioGroup);
						switch (wf.getCheckedRadioButtonId()) {
							case R.id.flashModeTorch:
								flashMode = Parameters.FLASH_MODE_TORCH; break;
							case R.id.flashModeOff:
								flashMode = Parameters.FLASH_MODE_OFF; break;
							case R.id.flashModeRedEye:
								flashMode = Parameters.FLASH_MODE_RED_EYE; break;
							case R.id.flashModeOn:
							default:
								flashMode = Parameters.FLASH_MODE_ON; break;
						}
						Log.d("StillCamera.SettingsDialog", "Setting flash mode to " + flashMode);
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						SettingsDialog.this.getDialog().cancel();
					}
				});
			
			return builder.create();
		}
	}
}
