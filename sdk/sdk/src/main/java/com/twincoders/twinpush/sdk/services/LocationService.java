package com.twincoders.twinpush.sdk.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.util.LocationUpdateRequester;

public class LocationService extends Service {

	protected PendingIntent locationListenerPassivePendingIntent;

	LocationManager locationManager;
	LocationChangeReceiver locaitonChangeReceiver = new LocationChangeReceiver();

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
		Intent passiveIntent = new Intent(this, LocationChangeReceiver.class);
		locationListenerPassivePendingIntent = PendingIntent.getBroadcast(this, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		// Passive location updates from 3rd party apps when the Activity isn't visible.
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationUpdateRequester locationUpdateRequester = new LocationUpdateRequester(locationManager);
		locationUpdateRequester.requestPassiveLocationUpdates(minUpdateDistance, minUpdateTime, locationListenerPassivePendingIntent);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(locaitonChangeReceiver);
		super.onDestroy();
	}
}
