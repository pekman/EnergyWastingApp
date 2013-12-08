package com.github.robinbj86.energywastingapp.components;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import android.os.Environment;
import android.util.Log;

public class ExtStorageFileWriter extends Component {

	@Override
	public String getName() { return "Write file (all external storage)"; }

	@Override
	public boolean isSupported() {
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	private Vector<SpecifiedDirectoryFileWriter> writers =
			new Vector<ExtStorageFileWriter.SpecifiedDirectoryFileWriter>();

	/** File writer class for each external storage directory */
	private static class SpecifiedDirectoryFileWriter extends AbstractFileWriter {
		private File directory;

		public SpecifiedDirectoryFileWriter(File directory) {
			this.directory = directory;
		}

		@Override
		public String getName() { return "(used internally)"; }

		@Override
		protected File getPath() {
			return directory;
		}
	}

	@Override
	public void start() {
		Map<String, File> dirs = getAllStorageLocations();
		
		StringBuilder s = new StringBuilder("Found " + dirs.size() + " external storage locations:");
		for (Map.Entry<String, File> entry : dirs.entrySet()) {
			s.append("\n    " + entry.getKey() + ": " + entry.getValue());
		}
		Log.d("ExtStorageFileWriter", s.toString());
		
		for (File dir : dirs.values()) {
			SpecifiedDirectoryFileWriter writer = new SpecifiedDirectoryFileWriter(dir);
			writers.add(writer);
			Log.d("ExtStorageFileWriter", "Starting writer for " + dir);
			writer.start();
		}
	}

	@Override
	public void stop() {
		for (SpecifiedDirectoryFileWriter writer : writers) {
			Log.d("ExtStorageFileWriter", "Stopping writer for " + writer.directory);
			writer.stop();
		}
		writers.clear();
	}

	
	private static final String SD_CARD = "sdCard";
	private static final String EXTERNAL_SD_CARD = "externalSdCard";

	/**
	 * Finds external storage locations.
	 *
	 * This code is copied from
	 * <a href="http://stackoverflow.com/a/15612964">this stackoverflow.com message</a>.
	 *
	 * @return A map of all storage locations available
	 */
	private static Map<String, File> getAllStorageLocations() {
		Map<String, File> map = new HashMap<String, File>(10);

		List<String> mMounts = new ArrayList<String>(10);
		List<String> mVold = new ArrayList<String>(10);
		mMounts.add("/mnt/sdcard");
		mVold.add("/mnt/sdcard");

		try {
			File mountFile = new File("/proc/mounts");
			if(mountFile.exists()){
				Scanner scanner = new Scanner(mountFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					if (line.startsWith("/dev/block/vold/")) {
						String[] lineElements = line.split(" ");
						String element = lineElements[1];

						// don't add the default mount path
						// it's already in the list.
						if (!element.equals("/mnt/sdcard"))
							mMounts.add(element);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			File voldFile = new File("/system/etc/vold.fstab");
			if(voldFile.exists()){
				Scanner scanner = new Scanner(voldFile);
				while (scanner.hasNext()) {
					String line = scanner.nextLine();
					if (line.startsWith("dev_mount")) {
						String[] lineElements = line.split(" ");
						String element = lineElements[2];

						if (element.contains(":"))
							element = element.substring(0, element.indexOf(":"));
						if (!element.equals("/mnt/sdcard"))
							mVold.add(element);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


		for (int i = 0; i < mMounts.size(); i++) {
			String mount = mMounts.get(i);
			if (!mVold.contains(mount))
				mMounts.remove(i--);
		}
		mVold.clear();

		List<String> mountHash = new ArrayList<String>(10);

		for(String mount : mMounts){
			File root = new File(mount);
			if (root.exists() && root.isDirectory() && root.canWrite()) {
				File[] list = root.listFiles();
				String hash = "[";
				if(list!=null){
					for(File f : list){
						hash += f.getName().hashCode()+":"+f.length()+", ";
					}
				}
				hash += "]";
				if(!mountHash.contains(hash)){
					String key = SD_CARD + "_" + map.size();
					if (map.size() == 0) {
						key = SD_CARD;
					} else if (map.size() == 1) {
						key = EXTERNAL_SD_CARD;
					}
					mountHash.add(hash);
					map.put(key, root);
				}
			}
		}

		mMounts.clear();

		if(map.isEmpty()){
			map.put(SD_CARD, Environment.getExternalStorageDirectory());
		}
		return map;
	}

}
