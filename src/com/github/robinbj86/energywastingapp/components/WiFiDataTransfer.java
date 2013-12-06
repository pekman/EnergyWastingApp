package com.github.robinbj86.energywastingapp.components;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

import android.util.Log;


public class WiFiDataTransfer extends Component {

	public static final String SERVERIP = "192.168.0.3"; 
	public static final int SERVERPORT = 4444; 
	private static DatagramSocket socket = null;
	private static InetAddress serverAddr = null;
	private Thread sendingThread = null;
	private static boolean running = false;
	
	@Override
	public String getName() { return "WiFiDataTransfer"; }

	@Override
	public void start() {
		Log.d("WiFiDataTransfer", "Client: Starting the client");
		WiFiDataTransfer.running = true;
		try
		{
			if(null == socket){
				socket = new DatagramSocket();
			}
			
			if(null == serverAddr){
				serverAddr = InetAddress.getByName(SERVERIP); 
			}

			sendingThread = new Thread() {
				@Override
				public void run() {
					sendUDPDataToAddress(serverAddr, socket);
				};
			};

			sendingThread.start();

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void stop() {
		Log.d("WiFiDataTransfer", "Client: Stopping the client");
		WiFiDataTransfer.running = false;
		if(null != sendingThread && sendingThread.isAlive()){
			sendingThread.interrupt();	
		}
		socket = null;
		serverAddr = null;
	}

	@Override
	public void onPause() {
		Log.d("WiFiDataTransfer", "Client: Pause Called");
		if(!WiFiDataTransfer.running){
			markTurnedOff();
		}
	}
	
	private void sendUDPDataToAddress(InetAddress serverAddr, DatagramSocket socket) {
		byte[] buf = new byte[1400];
		new Random().nextBytes(buf);
		while(WiFiDataTransfer.running){
				try
				{
					DatagramPacket packet = new DatagramPacket(buf,buf.length, serverAddr, SERVERPORT); 
					socket.send(packet);
					Log.d("WiFiDataTransfer", "Client: Sending Succeeded");
				} catch (IOException e) {
					Log.e("WiFiDataTransfer", "Client: Sending Failed");
					e.printStackTrace();
				} 
		}
	}
}
