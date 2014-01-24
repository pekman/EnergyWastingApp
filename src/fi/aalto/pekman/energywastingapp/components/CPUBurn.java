package fi.aalto.pekman.energywastingapp.components;

import android.os.Process;
import android.util.Log;
import android.widget.Toast;

public class CPUBurn extends Component {

	@Override
	public String getName() { return "CPUburn"; }
	
	private static final boolean supported;
	private static final int SIGSTOP;
	private static final int SIGCONT;
	
	@Override
	public boolean isSupported() { return supported; }
	
	
	private static final int PERIOD = 500; // milliseconds
	
	private volatile int onTime = PERIOD;
	private LimiterThread thread = null;
	
	private int[] processes;
	
	private class LimiterThread extends Thread {
		
		public volatile boolean stop = false;
		
		@Override
		public synchronized void run() {
			while (! stop) {
				if (onTime == PERIOD) {
					// onTime == 100% => don't send signals, wait for change
					try {
						wait();
					} catch (InterruptedException e) {}
				}
				else {
					// onTime < 100%
					
					// stop processes for (PERIOD - onTime) milliseconds
					for (int pid : processes)
						Process.sendSignal(pid, SIGSTOP);
					
					try {
						wait(PERIOD - onTime);
					} catch (InterruptedException e) {}
					if (stop)
						break;
					
					// start processes for onTime milliseconds
					for (int pid : processes)
						Process.sendSignal(pid, SIGCONT);
					
					try {
						wait(onTime);
					} catch (InterruptedException e) {}
				}
			}
		}
	}
	
	@Override
	public void start() {
		if (processes != null)
			return;
		
		int numCores = getNumCores();
		if (numCores == -1) {
			Log.w("CPUBurn", "Unable to determine number of cores. Using 1 as fallback value.");
			numCores = 1;
		}
		else {
			Log.d("CPUBurn", numCores + " cores");
		}
		
		// start a process for each CPU core and store the PIDs
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
		
		// start limiter thread
		thread = new LimiterThread();
		thread.start();
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
		
		synchronized (thread) {
			thread.stop = true;
			thread.notify();
		}
		
		processes = null;
		thread = null;
	}
	
	@Override public boolean isAdjustable() { return true; }
	@Override public int getAdjustmentMin() { return 1; }
	@Override public int getAdjustmentMax() { return PERIOD; }
	
	@Override
	protected void onAdjustmentChange(int value) {
		onTime = value;
		
		if (thread != null) {
			synchronized (thread) {
				thread.notify();
			}
		}
	}
	
	
	/** Starts a new CPUburn process and returns its PID or -1 on error */
	private native int startCPUBurn();
	
	/**
	 * Returns the number of processor cores.
	 * 
	 * @return the number of cores, or -1 if there was a problem
	 */
	private native int getNumCores();
	
	private native static int getSIGSTOP();
	private native static int getSIGCONT();
	
	private native static boolean checkNeonSupport();
	
	static {
		// Try to load native library. If succeeded, check if the CPU supports
		// ARM NEON instructions, which are required by the native code.
		boolean success;
		try {
			System.loadLibrary("cpuburn");
			success = checkNeonSupport();
		} catch (UnsatisfiedLinkError e) {
			success = false;
		}
		
		if (success) {
			SIGSTOP = getSIGSTOP();
			SIGCONT = getSIGCONT();
		} else {
			SIGSTOP = SIGCONT = -1;
		}
		
		supported = success;
	}

}
