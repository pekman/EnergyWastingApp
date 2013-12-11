package com.github.robinbj86.energywastingapp.components;

import java.util.Random;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;


public class TonePlay extends Component {

	@Override
	public String getName() { 
		return "TonePlay";
	}

	public static final int WHITE_NOISE = 0;
	public static final int SINE_WAVE = 1;
	public static final int SQUARE_WAVE = 2;

	private static final int SAMPLE_RATE =
			AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

	public static int waveform = WHITE_NOISE;
	public static int frequency = 10000;
	private AudioTrack track = null;

	@Override
	public void start() {
		int numSamples;
		
		// generate audio
		Log.d("TonePlay", "Generating audio (sample rate " + SAMPLE_RATE + " Hz)");
		switch (waveform) {
		case WHITE_NOISE:
			numSamples = SAMPLE_RATE * 2;
			track = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					SAMPLE_RATE,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					numSamples * 2,  // 1 second
					AudioTrack.MODE_STATIC );
			{
				byte[] buffer = new byte[numSamples * 2];
				new Random().nextBytes(buffer);
				track.write(buffer, 0, numSamples * 2);
			}
			Log.d("TonePlay", "White noise");
			break;
		
		case SINE_WAVE:
		case SQUARE_WAVE:
			numSamples = SAMPLE_RATE / frequency;
			track = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					SAMPLE_RATE,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					numSamples * 2,
					AudioTrack.MODE_STATIC );
			
			{
				short[] buffer = new short[numSamples];
				if (waveform == SINE_WAVE) {
					Log.d("TonePlay", "Sine wave " + frequency + " Hz");
					for (int i=0; i<numSamples; i++) {
						double angle = 2.0 * Math.PI * ((float) i) / ((float) numSamples);
						buffer[i] = (short) (Short.MAX_VALUE * ((float) Math.sin(angle)));
					}
				}
				else { // SQUARE_WAVE
					Log.d("TonePlay", "Square wave " + frequency + " Hz");
					int i=0;
					while (i < numSamples/2)
						buffer[i++] = Short.MAX_VALUE;
					while (i < numSamples)
						buffer[i++] = Short.MIN_VALUE;
				}
				track.write(buffer, 0, numSamples);
			}
			Log.d("TonePlay", "" + numSamples + " samples (" +
					(numSamples / (SAMPLE_RATE/1000.0)) + " ms)");
			break;
			
		default:
			return;
		}
		
		// loop forever
		track.setLoopPoints(0, numSamples, -1);
		
		// start playing
		track.play();
	}

	@Override
	public void stop() {
		if (track != null) {
			track.stop();
			track.release();
			track = null;
		}
	}

}
