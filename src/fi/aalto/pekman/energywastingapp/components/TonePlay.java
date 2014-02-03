package fi.aalto.pekman.energywastingapp.components;

import java.util.Random;

import fi.aalto.pekman.energywastingapp.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class TonePlay extends Component {

	@Override
	public String getName() { 
		return "TonePlay";
	}

	private static final int WHITE_NOISE = 0;
	private static final int SINE_WAVE = 1;
	private static final int SQUARE_WAVE = 2;

	public static final int SAMPLE_RATE =
			AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
	private static final int MAX_NUM_SAMPLES = SAMPLE_RATE;

	private static int waveform = WHITE_NOISE;
	private static int frequency = 11025;

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

	@Override
	public DialogFragment getSettingsDialog() {
		return new SettingsDialog();
	}

	public static class SettingsDialog extends DialogFragment {

		/** Moves seekbar when textbox is edited */
		private static class EditFreqTextWatcher implements TextWatcher {
			
			private final SeekBar freqSeekBar;
			
			public EditFreqTextWatcher(SeekBar freqSeekBar) {
				this.freqSeekBar = freqSeekBar;
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				try {
					int freq = Integer.parseInt(s.toString());
					freqSeekBar.setProgress(freq - 1);
				} catch (NumberFormatException e) {}
			}
		}
		
		/** Updates textbox value when seekbar is moved */
		private static class FreqSeekBarChangeListener implements OnSeekBarChangeListener {
			
			private final EditText editFreq;
			
			public FreqSeekBarChangeListener(EditText editFreq) {
				this.editFreq = editFreq;
			}
			
			@Override public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					int freq = progress + 1;
					editFreq.setText(Integer.toString(freq));
				}
			}
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_tone_settings, null);
			
			int id;
			switch (waveform) {
				case WHITE_NOISE: id = R.id.waveformWhiteNoise; break;
				case SINE_WAVE:   id = R.id.waveformSineWave;   break;
				case SQUARE_WAVE: id = R.id.waveformSquareWave; break;
				default: id = -1; break;
			}
			((RadioGroup) view.findViewById(R.id.waveformRadioGroup)).check(id);
			
			EditText editFreq = (EditText) view.findViewById(R.id.editFreq);
			editFreq.setText(Integer.toString(frequency));
			
			SeekBar freqSeekBar = (SeekBar) view.findViewById(R.id.freqSeekBar);
			freqSeekBar.setMax((SAMPLE_RATE / 2) - 1);
			freqSeekBar.setProgress(frequency - 1);
			freqSeekBar.setOnSeekBarChangeListener(new FreqSeekBarChangeListener(editFreq));
			
			editFreq.addTextChangedListener(new EditFreqTextWatcher(freqSeekBar));
			
			builder.setTitle("TonePlay settings")
				.setView(view)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						RadioGroup wf = (RadioGroup)
								((Dialog) dialog).findViewById(R.id.waveformRadioGroup);
						switch (wf.getCheckedRadioButtonId()) {
							case R.id.waveformSineWave:   waveform = SINE_WAVE;   break;
							case R.id.waveformSquareWave: waveform = SQUARE_WAVE; break;
							default:
							case R.id.waveformWhiteNoise: waveform = WHITE_NOISE; break;
						}
						
						EditText t = (EditText) ((Dialog) dialog).findViewById(R.id.editFreq);
						frequency = Integer.parseInt(t.getText().toString());
						Log.d("TonePlay.SettingsDialog",
								"Setting frequency to " + frequency + " Hz");
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
