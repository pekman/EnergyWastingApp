package com.github.robinbj86.energywastingapp.components;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import android.util.Log;
import android.widget.Toast;

public class CPUBurn extends Component {

	public String getName() { return "CPUburn"; }
	
	private int[] processes;
	
	@Override
	public void start() {
		if (processes != null)
			return;
		
		// start a process for each CPU core and store the PIDs
		int numCores = getNumCores();
		processes = new int[numCores];
		for (int i=0; i<numCores; i++) {
			int pid = 0;
			try {
				pid = startCPUBurn();
				
				if (pid > 0) {
					Log.d("CPUBurn", "forked process (PID=" + pid + ")");
				}
				else {
					Log.e("CPUBurn", "error starting process");
				}
			} catch (UnsatisfiedLinkError e) {
				Log.e("CPUBurn", "error calling native method startCPUBurn()", e);
				pid = 0;
			}
			if (pid <= 0) {
				Toast.makeText(context, "error starting cpuburn process", Toast.LENGTH_LONG)
					.show();
			}
			
			processes[i] = pid;
		}
	}
	
	@Override
	public void stop() {
		if (processes == null)
			return;
		
		for (int pid : processes) {
			if (pid > 0) {
				android.os.Process.killProcess(pid);
				Log.d("CPUBurn", "stopped process (PID=" + pid + ")");
			}
		}
		processes = null;
	}
	
	
	/**
	 * Gets the number of cores available in this device, across all processors.
	 * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
	 *
	 * This code is copied from
	 * <a href="http://forums.makingmoneywithandroid.com/android-development/280-%5Bhow-%5D-get-number-cpu-cores-android-device.html"
	 *    >this forum post</a>.
	 *
	 * @return The number of cores, or 1 if failed to get result
	 */
	private int getNumCores() {

		//Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				//Check if filename is "cpu", followed by a single digit number
				if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
					return true;
				}
				return false;
			}
		}
		
		try {
			//Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			//Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			Log.d("getNumCores", "CPU Count: "+files.length);
			//Return the number of cores (virtual CPU devices)
			return files.length;
		} catch(Exception e) {
			//Print exception
			Log.d("getNumCores", "CPU Count: Failed.");
			e.printStackTrace();
			//Default to return 1 core
			return 1;
		}
	}
	

	/** Starts a new CPUburn process and returns its PID or -1 on error */
	private native int startCPUBurn();
	
	static {
		System.loadLibrary("arm-cpuburn");
	}

}
