package com.github.robinbj86.energywastingapp.components;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;



public class GPSCoordSearch extends Component implements LocationListener{


	private LocationManager lm = null;
	private static boolean running = false;
	
	@Override
	public String getName() { return "GPSSearch"; }

	@Override
	public void start() {
		lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			markTurnedOn();
			running = true;
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		} else {
			context.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 100);
			if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				running = true;
				markTurnedOn();
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			} else {
				running = false;
				Log.e("GPSCoordSearch", "GPS cannot be turned on!");
				markTurnedOff();
			}
		}
		
	}

	@Override
	public void stop() {
		if(null != lm && GPSCoordSearch.running){
			lm.removeUpdates(this);
			markTurnedOff();
		}
	}

	@Override
	public void onPause() {
		if(!GPSCoordSearch.running){
			markTurnedOff();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	
}
