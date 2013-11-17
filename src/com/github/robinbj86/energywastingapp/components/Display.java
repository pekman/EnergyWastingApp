package com.github.robinbj86.energywastingapp.components;

import com.github.robinbj86.energywastingapp.R;

import android.app.ActionBar;
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

	@Override
	public String getName() { return "Display"; }

	@Override
	public void start() {
		// set brightness mode to manual if it's automatic
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
		
		// set brightness to maximum, keep screen on, and turn on fullscreen
		WindowManager.LayoutParams layoutParams = context.getWindow().getAttributes();
		savedBrightness = layoutParams.screenBrightness;
		savedFlags = layoutParams.flags;
		layoutParams.screenBrightness = 1.0F;
		layoutParams.flags |=
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_FULLSCREEN;
		context.getWindow().setAttributes(layoutParams);
		
		// white screen for max power consumption with OLED screens:
		
		// make WhiteScreen visible
		View whiteScreen = context.findViewById(R.id.WhiteScreen);
		whiteScreen.setVisibility(View.VISIBLE);
		// hide action bar
		ActionBar ab = context.getActionBar();
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

	@Override
	public void stop() {
		// restore screen state
		if (isStateSaved) {
			Settings.System.putInt(
					context.getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS_MODE,
					savedBrightnessMode );
			
			WindowManager.LayoutParams layoutParams = context.getWindow().getAttributes();
			layoutParams.screenBrightness = savedBrightness;
			layoutParams.flags = savedFlags;
			context.getWindow().setAttributes(layoutParams);
			
			if (savedActionBarShowing)
				context.getActionBar().show();
			
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

}
