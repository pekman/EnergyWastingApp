package fi.aalto.pekman.energywastingapp.components;

import android.content.Context;
import android.os.Vibrator;

public class Vibration extends Component {

	private static final int PERIOD = 20;  // in ms
	
	private static final Vibrator vibrator = (Vibrator)
			context.getSystemService(Context.VIBRATOR_SERVICE);
	
	private int dutyCycle = getAdjustmentMax();
	
	@Override
	public String getName() { return "Vibration"; }
	
	@Override
	public boolean isSupported() {
		return vibrator.hasVibrator();
	}
	
	@Override
	public void start() {
		long[] pattern = new long[] { PERIOD - dutyCycle, dutyCycle };
		vibrator.vibrate(pattern, 0);
	}
	
	@Override
	public void stop() {
		vibrator.cancel();
	}
	
	@Override public boolean isAdjustable() { return true; }
	@Override public int getAdjustmentMax() { return PERIOD; }
	
	@Override
	protected void onAdjustmentChange(int value) {
		dutyCycle = value;
		if (running) {
			stop();
			start();
		}
	}

}
