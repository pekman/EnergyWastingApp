package com.github.robinbj86.energywastingapp.components;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.util.Log;

public class RecordAudio extends Component {

	@Override
	public String getName() { return "Record audio"; }

	private MediaRecorder rec = null;

	@Override
	public boolean isSupported() {
		return context.getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
	}

	@Override
	public void start() {
		if (rec == null && isSupported()) {
			rec = new MediaRecorder();
			rec.setAudioSource(MediaRecorder.AudioSource.MIC);
			rec.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			rec.setAudioSamplingRate(96000);
			rec.setAudioChannels(2);
			rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			rec.setOutputFile("/dev/null");
			
			try {
				rec.prepare();
			} catch (Exception e) {
				Log.e("RecordAudio.start()", e.getMessage(), e);
				rec = null;
				stop();
				markTurnedOff();
				return;
			}
			rec.start();
		}
		else
			markTurnedOff();
		
		Log.d("RecordAudio", "Recording audio from microphone");
	}

	@Override
	public void stop() {
		if (rec != null) {
			Log.d("RecordAudio", "Stopping audio recording");
			
			rec.stop();
			rec.release();
			rec = null;
		}
	}

	@Override
	public void onPause() {
		if (running) {
			stop();
		}
	}

	@Override
	public void onResume() {
		if (running) {
			start();
		}
	}
}
