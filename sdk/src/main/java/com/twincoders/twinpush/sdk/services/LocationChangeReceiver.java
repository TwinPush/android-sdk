package com.twincoders.twinpush.sdk.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.util.LastLocationFinder;

public class LocationChangeReceiver extends BroadcastReceiver {

    /**
     * When a new location is received, extract it from the Intent and use it to start the Service used to update the list of nearby places.
     *
     * This is the Passive receiver, used to receive Location updates from third party apps when the Activity is not visible.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Ln.d("Location update received");
        TwinPushSDK twinPush = TwinPushSDK.getInstance(context);
        String key = LocationManager.KEY_LOCATION_CHANGED;
        Location location;

        if (intent.hasExtra(key)) {
            // This update came from Passive provider, so we can extract the location directly.
            location = (Location) intent.getExtras().get(key);
        } else {
            // Get the best last location detected from the providers.
            LastLocationFinder lastLocationFinder = new LastLocationFinder(context);
            location = lastLocationFinder.getLastBestLocation(twinPush.getLocationMinUpdateDistance(), System.currentTimeMillis() - twinPush.getLocationMinUpdateTime());
        }
        if (location != null) {
            twinPush.setLocation(location);
        }
    }
}