package com.github.robinbj86.energywastingapp.components;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceHolder;

public class VideoCamera extends AbstractCamera {

	@Override
	public String getName() { return "Camera (video) & flash"; }

	private MediaRecorder recorder = null;

	/** UI component that shows the preview image from camera */
	private class Preview extends AbstractCamera.Preview {
		
		protected void onStart(SurfaceHolder holder) {
			cam.unlock();
			
			recorder = new MediaRecorder();
			recorder.setCamera(cam);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			
			// record video only with the highest supported profile
			CamcorderProfile p = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
			recorder.setOutputFormat(p.fileFormat);
			recorder.setVideoFrameRate(p.videoFrameRate);
			recorder.setVideoSize(p.videoFrameWidth, p.videoFrameHeight);
			recorder.setVideoEncodingBitRate(p.videoBitRate);
			recorder.setVideoEncoder(p.videoCodec);
			
			recorder.setOutputFile("/dev/null");
			recorder.setPreviewDisplay(holder.getSurface());
			
			try {
				recorder.prepare();
			} catch (Exception e) {
				Log.e("VideoCamera.Preview.onStart()", e.getMessage(), e);
				recorder = null;
				onError();
				return;
			}
			recorder.start();
			
			Log.d("VideoCamera", "Recording video");
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
			
			// create and add preview UI component
			addPreview(new Preview());
		}
		else
			markTurnedOff();
	}

	@Override
	public void stop() {
		Log.d("VideoCamera", "Stopping video recording");
		
		if (recorder != null) {
			recorder.stop();
			recorder.release();
		}
		if (cam != null) {
			cam.lock();
		}
		
		super.stop();
	}

}
