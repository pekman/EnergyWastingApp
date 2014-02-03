package fi.aalto.pekman.energywastingapp.components;

import fi.aalto.pekman.energywastingapp.R;

import android.annotation.TargetApi;
import android.support.v7.app.ActionBar;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class Display extends Component {

	private boolean isStateSaved = false;
	private int savedBrightnessMode;
	private float savedBrightness;
	private int savedFlags;
	private boolean savedActionBarShowing;
	private float brightness = 1.0F;

	@Override
	public String getName() { return "Display"; }

	@TargetApi(8)
	@Override
	public void start() {
		
		// set brightness mode to manual if it's automatic
		if (Build.VERSION.SDK_INT >= 8) {
			int brightnessMode;
			try {
				brightnessMode = Settings.System.getInt(
						context.getContentResolver(),
						Settings.System.SCREEN_BRIGHTNESS_MODE);
				savedBrightnessMode = brightnessMode;
			} catch (SettingNotFoundException e) {
				Log.e("Display.start()", e.toString(), e);
				markTurnedOff();
				return;
			}
			
			if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
				Settings.System.putInt(
						context.getContentResolver(),
						Settings.System.SCREEN_BRIGHTNESS_MODE,
						Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL );
			}
		}
		
		// set brightness, keep screen on, and turn on fullscreen
		WindowManager.LayoutParams layoutParams = context.getWindow().getAttributes();
		savedBrightness = layoutParams.screenBrightness;
		savedFlags = layoutParams.flags;
		layoutParams.screenBrightness = brightness;
		layoutParams.flags |=
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_FULLSCREEN;
		context.getWindow().setAttributes(layoutParams);
		
		// white screen for max power consumption with OLED screens:
		
		// make WhiteScreen visible
		View whiteScreen = context.findViewById(R.id.WhiteScreen);
		whiteScreen.setVisibility(View.VISIBLE);
		// hide action bar
		ActionBar ab = context.getSupportActionBar();
		savedActionBarShowing = ab.isShowing();
		if (savedActionBarShowing)
			ab.hide();
		// set onClick listener to turn WhiteScreen off
		whiteScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				stop();
				markTurnedOff();
			}
		});
		
		isStateSaved = true;
	}

	@TargetApi(8)
	@Override
	public void stop() {
		// restore screen state
		if (isStateSaved) {
			if (Build.VERSION.SDK_INT >= 8) {
				Settings.System.putInt(
						context.getContentResolver(),
						Settings.System.SCREEN_BRIGHTNESS_MODE,
						savedBrightnessMode );
			}
			
			WindowManager.LayoutParams layoutParams = context.getWindow().getAttributes();
			layoutParams.screenBrightness = savedBrightness;
			layoutParams.flags = savedFlags;
			context.getWindow().setAttributes(layoutParams);
			
			if (savedActionBarShowing)
				context.getSupportActionBar().show();
			
			isStateSaved = false;
		}
		
		// hide WhiteScreen
		View whiteScreen = context.findViewById(R.id.WhiteScreen);
		whiteScreen.setVisibility(View.GONE);
	}

	@Override
	public void onPause() {
		stop();
		markTurnedOff();
	}

	@Override public boolean isAdjustable() {
		// brightness can only be adjusted on API level >= 8
		return Build.VERSION.SDK_INT >= 8;
	}
	@Override public int getAdjustmentMin() { return 10; }
	@Override public int getAdjustmentMax() { return 100; }
	
	@Override
	protected void onAdjustmentChange(int value) {
		brightness = value / 100.0F;
	}

}
