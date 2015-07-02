package com.twincoders.twinpush.sdk.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;

public class LocationService extends Service implements LocationListener {

	LocationManager locationManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Ln.i("Location service started");

		TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());
		int minUpdateDistance = twinPush.getLocationMinUpdateDistance();
		long minUpdateTime = twinPush.getLocationMinUpdateTime();
		// Passive location updates from 3rd party apps when the Activity isn't visible.
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minUpdateTime, minUpdateDistance, this);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
		super.onDestroy();
	}
	
	/* LocationListener */
	
	@Override
    public void onLocationChanged(Location location) {
    	Ln.d("Updated location");
    	TwinPushSDK.getInstance(getApplicationContext()).setLocation(location);
    }

    @Override public void onStatusChanged(String s, int i, Bundle bundle) { }
    @Override public void onProviderEnabled(String s) { }
    @Override public void onProviderDisabled(String s) { }
}
