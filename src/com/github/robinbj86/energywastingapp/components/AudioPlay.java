package com.github.robinbj86.energywastingapp.components;

import com.github.robinbj86.energywastingapp.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;


public class AudioPlay extends Component {
	
	private static final String TAG = null;
	private MediaPlayer player;

	private static boolean running = false;
	
	@Override
	public String getName() { 
		return "AudioPlay";
	}

	@Override
	public void start() {
		running = true;
		int resID = R.raw.a;
		AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		player = MediaPlayer.create(context, resID);
		player.setLooping(true);
		player.start();

	}

	@Override
	public void stop() {
		if(player.isPlaying() && running) {
			player.stop();
			player.release();
			running = false;
		}
	}

	@Override
	public void onPause() {
		if(running){
			markTurnedOn();
		} else {
			markTurnedOff();
		}
	}

	
}
