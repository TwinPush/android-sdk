/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import com.twincoders.twinpush.sdk.logging.Ln;

import java.util.List;

/**
 * Optimized implementation of Last Location Finder for devices running Gingerbread  
 * and above.
 *
 * This class let's you find the "best" (most accurate and timely) previously 
 * detected location using whatever providers are available. 
 *
 * Where a timely / accurate previous location is not detected it will
 * return the newest location (where one exists) and setup a oneshot 
 * location update to find the current location.
 */
public class LastLocationFinder {

    protected static String SINGLE_LOCATION_UPDATE_ACTION = "com.twincoders.twinpush.sdk.SINGLE_LOCATION_UPDATE_ACTION";

    protected PendingIntent singleUpatePI;
    protected LocationListener locationListener;
    protected LocationManager locationManager;
    protected Context context;
    protected Criteria criteria;

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
     * specified in {@link setChangedLocationListener}.
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
        // This check simply implements the same conditions we set when requesting regular
        // location updates every [minTime] and [minDistance].
        if (android.os.Build.VERSION.SDK_INT >= 9 && locationListener != null && (bestTime < minTime || bestAccuracy > minDistance)) {
            IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
            context.registerReceiver(singleUpdateReceiver, locIntentFilter);
            try {
                locationManager.requestSingleUpdate(criteria, singleUpatePI);
            } catch (Exception ex) {
                Ln.e("Error while trying to update location: %s", ex.getLocalizedMessage());
            }
        }

        return bestResult;
    }

    /**
     * This {@link BroadcastReceiver} listens for a single location
     * update before unregistering itself.
     * The oneshot location update is returned via the {@link LocationListener}
     * specified in {@link setChangedLocationListener}.
     */
    protected BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(singleUpdateReceiver);

            String key = LocationManager.KEY_LOCATION_CHANGED;
            Location location = (Location)intent.getExtras().get(key);

            if (locationListener != null && location != null)
                locationListener.onLocationChanged(location);

            locationManager.removeUpdates(singleUpatePI);
        }
    };

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
    }
}