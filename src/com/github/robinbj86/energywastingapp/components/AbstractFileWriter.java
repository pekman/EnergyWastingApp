package com.github.robinbj86.energywastingapp.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.util.Log;

public abstract class AbstractFileWriter extends Component {

	protected static final String filename = "tmpfile";
	protected static final int chunkSize = 65536;

	/** Returns the directory to which the file is written */
	protected abstract File getPath();

	/** Returns maximum file size */
	protected long getMaxFileSize() { return 128 * 1024 * 1024; }


	protected WriterThread thread = null;
	protected volatile boolean stopThread = false;

	protected static final byte[] buffer;
	static {
		buffer = new byte[chunkSize];
		new Random().nextBytes(buffer);
	}

	protected class WriterThread extends Thread {
		
		private void onError() {
			context.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					markTurnedOff();
				}
			});
		}
		
		@Override
		public void run() {
			Log.d("AbstractFileWriter thread", "Starting write thread");
			
			File file = new File(getPath(), filename);
			long maxSize = getMaxFileSize();
			
			while (! stopThread) {
				// open file for writing (truncate if it exists)
				FileOutputStream out;
				try {
					out = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					Log.e("AbstractFileWriter thread", e.getMessage(), e);
					onError();
					return;
				}
				Log.d("AbstractFileWriter thread", "File " + file.getAbsolutePath() + " opened");
				
				long size = 0;
				
				try {
					// write to file until size limit reached
					while (! stopThread && size <= maxSize) {
						out.write(buffer, 0, chunkSize);
						out.flush();
						out.getFD().sync();
						
						size += chunkSize;
					}
				} catch (IOException e) {
					Log.e("AbstractFileWriter thread", e.getMessage(), e);
					onError();
				} finally {
					Log.d("AbstractFileWriter thread", "Closing and deleting file (" + size + " bytes written)");
					
					// close and delete file
					try {
						out.close();
					} catch (IOException e) {
						Log.e("AbstractFileWriter thread", "close: " + e.getMessage(), e);
					}
					if (! file.delete()) {
						Log.w("AbstractFileWriter thread",
								"cannot delete " + file.getAbsolutePath());
					}
				}
			}
			
			Log.d("AbstractFileWriter thread",
					"Write thread stopped (" + file.getAbsolutePath() + ")");
		}
	}

	@Override
	public void start() {
		thread = new WriterThread();
		stopThread = false;
		thread.start();
	}

	@Override
	public void stop() {
		stopThread = true;
	}

}
