package com.yellowpineapple.offers101.models;

import android.location.Location;

import java.io.Serializable;

import lombok.Getter;

/**
 * Created by agutierrez on 04/02/15.
 */
public class Store implements Serializable {
    @Getter int id;
    @Getter String name;
    @Getter String address;
    @Getter double latitude;
    @Getter double longitude;

    public Location getLocation() {
        Location location = null;
        if (latitude != 0 || longitude != 0) {
            location = new Location(name);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
        }
        return location;
    }

    public static int LOCATION_INVALID = -1;
    public int getDistance(Location location) {
        int distance = LOCATION_INVALID;
        Location storeLocation = getLocation();
        if (storeLocation != null && location != null) {
            distance = Math.round(location.distanceTo(storeLocation));
        }
        return distance;
    }
}
