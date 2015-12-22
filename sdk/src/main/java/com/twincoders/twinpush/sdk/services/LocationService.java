package com.twincoders.twinpush.sdk.services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;

public class LocationService extends Service {

    protected PendingIntent broadcastIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Ln.i("Location service started");
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());
            int minUpdateDistance = twinPush.getLocationMinUpdateDistance();
            long minUpdateTime = twinPush.getLocationMinUpdateTime();
            Intent passiveIntent = new Intent(this, LocationChangeReceiver.class);
            broadcastIntent = PendingIntent.getBroadcast(this, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Passive location updates from 3rd party apps when the Activity isn't visible.
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, minUpdateTime, minUpdateDistance, broadcastIntent);
            return START_STICKY;
        } else {
            stopSelf();
            Ln.e("Could not start location updates, required permissions not found");
            return START_NOT_STICKY;
        }
    }
}
