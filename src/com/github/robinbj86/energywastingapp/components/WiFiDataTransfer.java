package com.github.robinbj86.energywastingapp.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Random;
import android.util.Log;


public class WiFiDataTransfer extends Component {

	public static final String SERVERIP = "192.168.0.3"; 
	private URL url;
	public static final String urlString = "http://www.ndtv.com/sitemap.xml";
	public static final int SERVERPORT = 4444; 
	private static DatagramSocket socket = null;
	private static InetAddress serverAddr = null;
	private Thread sendingThread = null;
	private Thread downloadThread = null;
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
			
			if(null == url) {
				url = new URL(urlString);
			}

			sendingThread = new Thread() {
				@Override
				public void run() {
					sendUDPDataToAddress(serverAddr, socket);
				};
			};
			
			downloadThread = new Thread() {
				@Override
				public void run() {
					downloadDataToSDCard();
				};
			};

			sendingThread.start();
			downloadThread.start();

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
	
	private void downloadDataToSDCard() {
		HttpURLConnection urlConnection = null;
		File destinationFile = null;
		FileOutputStream fileOutput = null;
		InputStream inputStream = null;
		while(WiFiDataTransfer.running){
			try {
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				urlConnection.connect();
				destinationFile = new File("/dev/null");
				fileOutput = new FileOutputStream(destinationFile);
				inputStream = urlConnection.getInputStream();
				byte[] buffer = new byte[1024];

				int bufferLength = 0; 
				while ( (bufferLength = inputStream.read(buffer)) > 0 ) 
				{
					fileOutput.write(buffer, 0, bufferLength);
				}
			} catch (Exception e) {
				Log.e("WiFiDataTransfer", "Exception while getting data from url");
				e.printStackTrace();
			} finally {
				try {
					if(null != fileOutput){
						fileOutput.flush();
						fileOutput.close();
					}
					if(null != destinationFile){
						destinationFile.delete();
					}
					if(null != inputStream){
						inputStream.close();
					}
					if(null != urlConnection){
						urlConnection.disconnect();
						urlConnection = null;
					}
					
				} catch (IOException e) {
					Log.e("WiFiDataTransfer", "Exception cleaning up");
					e.printStackTrace();
				}
				
			}
		}

	}
}
