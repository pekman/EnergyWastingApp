package com.github.robinbj86.energywastingapp.components;

import android.bluetooth.*;
import android.content.Intent;
import android.util.Log;

public class BlueToothBurn extends Component {

	private volatile BluetoothAdapter bluetooth = null;
	private static final int REQUEST_ENABLE_BT = 1;
	private volatile static boolean running = false;
	
	@Override
	public String getName() { 
		return "BlueToothBurn";
	}

	@Override
	public void start() {
		
		if(null == bluetooth){
			bluetooth = BluetoothAdapter.getDefaultAdapter();
			if(bluetooth != null)
			{
				if (!bluetooth.isEnabled()) {
				    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				} else {
					markTurnedOn();
				}
				
				if(bluetooth.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
				{
					Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
					discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
					context.startActivity(discoverableIntent);
				}
				running = true;
				
				if(bluetooth.isEnabled()){
					Thread thread = new Thread() {
						@Override
						public void run() {
							while(running){
								try {
									Thread.sleep(30);
									if(bluetooth.isDiscovering()){
										bluetooth.cancelDiscovery();
									}
									bluetooth.startDiscovery();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							
							bluetooth = null;
						}
					};
					thread.start();
				}
				
			} else {
				Log.e("BlueToothBurn", "BlueTooth cannot be turned on!");
			}
		}
	}

	@Override
	public void stop() {
		running = false;
		markTurnedOff();
	}

	@Override
	public void onPause() {
		
	}

}
