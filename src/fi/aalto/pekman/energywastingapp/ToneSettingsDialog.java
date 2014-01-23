package fi.aalto.pekman.energywastingapp;

import fi.aalto.pekman.energywastingapp.R;
import fi.aalto.pekman.energywastingapp.components.TonePlay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ToneSettingsDialog extends DialogFragment {

	private static class EditFreqTextWatcher implements TextWatcher {
		
		private final SeekBar freqSeekBar;
		
		public EditFreqTextWatcher(SeekBar freqSeekBar) {
			this.freqSeekBar = freqSeekBar;
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			try {
				int freq = Integer.parseInt(s.toString());
				freqSeekBar.setProgress(freq - 1);
			} catch (NumberFormatException e) {}
		}
	}
	
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
		switch (TonePlay.waveform) {
			case TonePlay.WHITE_NOISE: id = R.id.waveformWhiteNoise; break;
			case TonePlay.SINE_WAVE:   id = R.id.waveformSineWave;   break;
			case TonePlay.SQUARE_WAVE: id = R.id.waveformSquareWave; break;
			default: id = -1; break;
		}
		((RadioGroup) view.findViewById(R.id.waveformRadioGroup)).check(id);
		
		EditText editFreq = (EditText) view.findViewById(R.id.editFreq);
		editFreq.setText(Integer.toString(TonePlay.frequency));
		
		SeekBar freqSeekBar = (SeekBar) view.findViewById(R.id.freqSeekBar);
		freqSeekBar.setMax((TonePlay.SAMPLE_RATE / 2) - 1);
		freqSeekBar.setProgress(TonePlay.frequency - 1);
		freqSeekBar.setOnSeekBarChangeListener(new FreqSeekBarChangeListener(editFreq));
		
		editFreq.addTextChangedListener(new EditFreqTextWatcher(freqSeekBar));
		
		builder.setTitle("TonePlay settings")
			.setView(view)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					RadioGroup wf = (RadioGroup)
							((Dialog) dialog).findViewById(R.id.waveformRadioGroup);
					int waveform;
					switch (wf.getCheckedRadioButtonId()) {
						case R.id.waveformSineWave:   waveform = TonePlay.SINE_WAVE;   break;
						case R.id.waveformSquareWave: waveform = TonePlay.SQUARE_WAVE; break;
						default:
						case R.id.waveformWhiteNoise: waveform = TonePlay.WHITE_NOISE; break;
					}
					TonePlay.waveform = waveform;
					
					EditText t = (EditText) ((Dialog) dialog).findViewById(R.id.editFreq);
					TonePlay.frequency = Integer.parseInt(t.getText().toString());
					Log.d("ToneSettingsDialog", "Setting frequency to " + TonePlay.frequency + " Hz");
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ToneSettingsDialog.this.getDialog().cancel();
				}
			});
		
		return builder.create();
	}

}
