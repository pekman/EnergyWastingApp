package com.github.robinbj86.energywastingapp.components;

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
		player = MediaPlayer.create(context, null);
        player.setLooping(true); // Set looping
        player.setVolume(100,100);
        player.start();
	}

	@Override
	public void stop() {
		if(player.isPlaying()) {
			player.release();
		}
	}

	@Override
	public void onPause() {
		
	}

	
}
