package com.twincoders.twinpush.sdk.util;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.twincoders.twinpush.sdk.logging.Ln;

import java.util.List;

/**
 * Optimized implementation of Last Location Finder for devices running Gingerbread  
 * and above.
 *
 * This class lets you find the "best" (most accurate and timely) previously 
 * detected location using whatever providers are available. 
 *
 * Where a timely / accurate previous location is not detected it will
 * return the newest location (where one exists) and set up a oneshot 
 * location update to find the current location.
 */
public class LastLocationFinder {

    protected static String SINGLE_LOCATION_UPDATE_ACTION = "com.twincoders.twinpush.sdk.SINGLE_LOCATION_UPDATE_ACTION";

    protected PendingIntent singleUpatePI;
    protected LocationListener locationListener;
    protected LocationManager locationManager;
    protected Context context;
    protected Criteria criteria;
    protected BroadcastReceiver singleUpdateReceiver;

    /**
     * Construct a new Gingerbread Last Location Finder.
     * @param context Context
     */
    public LastLocationFinder(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Coarse accuracy is specified here to get the fastest possible result.
        // The calling Activity will likely (or have already) request ongoing
        // updates using the Fine location provider.
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        // Construct the Pending Intent that will be broadcast by the oneshot
        // location update.
        Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_UPDATE_CURRENT;
        singleUpatePI = PendingIntent.getBroadcast(context, 0, updateIntent, flags);
    }

    /**
     * Returns the most accurate and timely previously detected location.
     * Where the last result is beyond the specified maximum distance or
     * latency a one-off location update is returned via the {@link LocationListener}
     * specified in setChangedLocationListener.
     * @param minDistance Minimum distance before we require a location update.
     * @param minTime Minimum time required between location updates.
     * @return The most accurate and / or timely previously detected location.
     */
    public Location getLastBestLocation(int minDistance, long minTime) {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider : matchingProviders) {
            boolean missingPermission =
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                    PackageManager.PERMISSION_GRANTED;
            if (missingPermission) {
                Ln.e("Missing required ACCESS_FINE_LOCATION permission to update location");
                return null;
            }
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                }
                else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        // If the best result is beyond the allowed time limit, or the accuracy of the
        // best result is wider than the acceptable maximum distance, request a single update.
        if (locationListener != null && (bestTime < minTime || bestAccuracy > minDistance)) {
            IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);

            singleUpdateReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // Unregister receiver immediately after receiving broadcast
                    context.unregisterReceiver(singleUpdateReceiver);

                    String key = LocationManager.KEY_LOCATION_CHANGED;
                    Location location = (Location) intent.getExtras().get(key);

                    if (locationListener != null && location != null) {
                        locationListener.onLocationChanged(location);
                    }

                    // Remove pending updates from the LocationManager
                    locationManager.removeUpdates(singleUpatePI);
                }
            };

            ContextCompat.registerReceiver(
                    context,
                    singleUpdateReceiver,
                    locIntentFilter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );

            try {
                locationManager.requestSingleUpdate(criteria, singleUpatePI);
            } catch (Exception ex) {
                Ln.e("Error while trying to update location: %s", ex.getLocalizedMessage());
            }
        }

        return bestResult;
    }

    /**
     * {@inheritDoc}
     */
    public void setChangedLocationListener(LocationListener l) {
        locationListener = l;
    }

    /**
     * {@inheritDoc}
     */
    public void cancel() {
        locationManager.removeUpdates(singleUpatePI);
        if (singleUpdateReceiver != null) {
            context.unregisterReceiver(singleUpdateReceiver);
            singleUpdateReceiver = null;
        }
    }
}
