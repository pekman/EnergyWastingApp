package com.github.robinbj86.energywastingapp.components;

import java.io.File;

public class AppDirFileWriter extends AbstractFileWriter {

	@Override
	public String getName() { return "Write file (app directory)"; }

	@Override
	protected File getPath() {
		return context.getCacheDir();
	}

}
