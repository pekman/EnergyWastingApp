package com.github.robinbj86.energywastingapp;

import com.github.robinbj86.energywastingapp.components.TonePlay;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

public class ToneSettingsDialog extends DialogFragment {

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
		
		((EditText) view.findViewById(R.id.editFreq)).setText(Integer.toString(TonePlay.frequency));
		
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
