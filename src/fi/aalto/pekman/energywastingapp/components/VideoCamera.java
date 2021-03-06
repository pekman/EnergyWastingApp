package fi.aalto.pekman.energywastingapp.components;

import android.annotation.TargetApi;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

public class VideoCamera extends AbstractCamera {

	@Override
	public String getName() { return "Camera (video) & flash"; }

	private MediaRecorder recorder = null;

	/** UI component that shows the preview image from camera */
	private class Preview extends AbstractCamera.Preview {
		
		@TargetApi(8)
		protected void onStart(SurfaceHolder holder) {
			cam.unlock();
			
			recorder = new MediaRecorder();
			recorder.setCamera(cam);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			
			// record video only with the highest supported profile
			if (Build.VERSION.SDK_INT >= 8) {
				CamcorderProfile p = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
				recorder.setOutputFormat(p.fileFormat);
				recorder.setVideoFrameRate(p.videoFrameRate);
				recorder.setVideoSize(p.videoFrameWidth, p.videoFrameHeight);
				recorder.setVideoEncodingBitRate(p.videoBitRate);
				recorder.setVideoEncoder(p.videoCodec);
			} else {
				recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
				recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
			}
			
			recorder.setOutputFile("/dev/null");
			recorder.setPreviewDisplay(holder.getSurface());
			
			try {
				recorder.prepare();
			} catch (Exception e) {
				Log.e("VideoCamera.Preview.onStart()", e.getMessage(), e);
				recorder.reset();
				recorder.release();
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
			recorder = null;
		}
		if (cam != null) {
			cam.lock();
		}
		
		super.stop();
	}

}
