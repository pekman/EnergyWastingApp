package com.github.robinbj86.energywastingapp.components;

import java.io.File;

import android.os.Environment;

public class ExtStorageFileWriter extends AbstractFileWriter {

	@Override
	public String getName() { return "Write file (external storage)"; }

	@Override
	public boolean isSupported() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	@Override
	protected File getPath() {
		return context.getExternalCacheDir();
	}

}
