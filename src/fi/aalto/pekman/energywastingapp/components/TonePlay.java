package fi.aalto.pekman.energywastingapp.components;

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

	public static final int SAMPLE_RATE =
			AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
	private static final int MAX_NUM_SAMPLES = SAMPLE_RATE;

	public static int waveform = WHITE_NOISE;
	public static int frequency = 11025;

	private AudioTrack track = null;
	private int volume = 100;

	/** Returns least common multiple of a and b */
	private static long lcm(int a, int b) {
		int gcd = a;
		int rem = b;
		while (rem > 0) {
			int tmp = rem;
			rem = gcd % rem;
			gcd = tmp;
		}
		
		return a * (long)(b / gcd);
	}

	@Override
	public void start() {
		int numSamples;
		short maxValue = (short)(((int)Short.MAX_VALUE * volume) / 100);
		short minValue = (short)(((int)Short.MIN_VALUE * volume) / 100);
		
		// generate audio
		Log.d("TonePlay", "Generating audio (sample rate " + SAMPLE_RATE + " Hz)");
		switch (waveform) {
		case WHITE_NOISE:
			Log.d("TonePlay", "White noise (volume " + volume + "%)");
			numSamples = SAMPLE_RATE;
			track = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					SAMPLE_RATE,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					numSamples * 2,  // 1 second
					AudioTrack.MODE_STATIC );
			{
				short[] buffer = new short[numSamples];
				Random rng = new Random();
				for (int i=0; i<numSamples; i++) {
					buffer[i] = (short)(rng.nextInt(maxValue*2 + 2) - maxValue - 1);
				}
				track.write(buffer, 0, numSamples);
			}
			break;
		
		case SINE_WAVE:
		case SQUARE_WAVE:
			long optimalNumSamples = lcm(SAMPLE_RATE, frequency*2) / frequency;
			numSamples = (int) Math.min(optimalNumSamples, MAX_NUM_SAMPLES);
			track = new AudioTrack(
					AudioManager.STREAM_MUSIC,
					SAMPLE_RATE,
					AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT,
					numSamples * 2,
					AudioTrack.MODE_STATIC );
			
			{
				double period = SAMPLE_RATE / (double)frequency;
				short[] buffer = new short[numSamples];
				if (waveform == SINE_WAVE) {
					Log.d("TonePlay", "Sine wave " + frequency + " Hz (volume " + volume + "%)");
					for (int i=0; i<numSamples; i++) {
						double angle = 2.0 * Math.PI * ((double) i) / period;
						buffer[i] = (short) (maxValue * ((float) Math.sin(angle)));
					}
				}
				else { // SQUARE_WAVE
					Log.d("TonePlay", "Square wave " + frequency + " Hz (volume " + volume + "%)");
					double halfPeriod = period / 2.0;
					for (int i=0; i < numSamples; i++) {
						double rem = ((double) i) % period;
						buffer[i] = (rem < halfPeriod) ? maxValue : minValue;
					}
				}
				track.write(buffer, 0, numSamples);
				Log.d("TonePlay", "period: " + period + " (" + (period/(SAMPLE_RATE/1000.0)) + " ms)");
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

	@Override public boolean isAdjustable() { return true; }
	@Override public int getAdjustmentMax() { return 100; }

	@Override
	public void onAdjustmentChange(int value) {
		volume = value;
		if (running) {
			stop();
			start();
		}
	}

}
