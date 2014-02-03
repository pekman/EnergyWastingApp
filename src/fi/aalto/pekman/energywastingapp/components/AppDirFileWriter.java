package fi.aalto.pekman.energywastingapp.components;

import java.io.File;

public class AppDirFileWriter extends AbstractFileWriter {

	@Override
	public String getName() { return "Write file (app directory)"; }

	@Override
	protected File getPath() {
		File dir = context.getCacheDir();
		if (dir == null)
			dir = context.getFilesDir();
		if (dir == null)
			dir = new File("/tmp");
		return dir;
	}

}
