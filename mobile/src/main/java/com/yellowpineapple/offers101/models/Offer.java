package com.yellowpineapple.offers101.models;

import android.content.Context;
import android.location.Location;

import com.yellowpineapple.offers101.R;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by agutierrez on 02/02/15.
 */
public class Offer implements Serializable {

    @Getter int id;
    @Getter boolean isOnline;
    @Getter @Setter Company company;
    @Getter Category category;
    @Getter String description;
    @Getter String shortDescription;
    @Getter String shortOffer;
    @Getter Date expirationDate;
    @Getter RemoteImage image;
    @Getter RemoteImage thumbnail;
    @Getter @Setter Store store;

    public boolean hasLocation() {
        return store != null && store.getLocation() != null;
    }

    private static int KM_LIMIT = 1000;

    public CharSequence getHumanizedDistance(Context context, Location currentLocation) {
        CharSequence distanceText;
        distanceText = context.getText(R.string.offer_distance_undefined);

        if (currentLocation != null) {
            if (hasLocation()) {
                int distance = store.getDistance(currentLocation);
                if (distance != Store.LOCATION_INVALID) {
                    if (distance < KM_LIMIT) {
                        distanceText = String.format(context.getText(R.string.offer_distance_x_meters).toString(), distance);
                    } else {
                        distanceText = String.format(context.getText(R.string.offer_distance_x_km).toString(), (distance / 1000f));
                    }
                }
            } else if (isOnline()) {
                distanceText = context.getText(R.string.offer_online);
            }
        }
        return distanceText;
    }

    public CharSequence getHumanizedExpiration(Context context) {
        CharSequence expirationText;
        if (expirationDate != null) {
            Calendar today = Calendar.getInstance();
            Calendar expiration = getExpirationTime();
            if (expiration.before(today)) {
                expirationText = context.getText(R.string.offer_expired);
            } else {
                long diff = expiration.getTimeInMillis() - today.getTimeInMillis();
                int dayDiff = Math.round(diff / (24 * 60 * 60 * 1000));
                switch (dayDiff) {
                    case 0: {
                        expirationText = context.getText(R.string.offer_expires_today);
                        break;
                    }
                    case 1: {
                        expirationText = context.getText(R.string.offer_expires_tomorrow);
                        break;
                    }
                    default: {
                        int monthDiff = dayDiff / 30;
                        if (monthDiff == 0) {
                            expirationText = String.format(context.getText(R.string.offer_expires_in_x_days).toString(), dayDiff);
                        } else if (monthDiff == 1) {
                            expirationText = context.getText(R.string.offer_expires_in_1_month);
                        } else if (monthDiff <= 12) {
                            expirationText = String.format(context.getText(R.string.offer_expires_in_x_months).toString(), monthDiff);
                        } else {
                            DateFormat df = new SimpleDateFormat(context.getText(R.string.offer_expiration_date_format).toString());
                            expirationText = df.format(expirationDate);
                        }
                    }
                }
            }
        } else {
            expirationText = context.getText(R.string.offer_expires_undefined);
        }
        return expirationText;
    }

    private Calendar getExpirationTime() {
        Calendar expiration = Calendar.getInstance();
        expiration.setTime(expirationDate);
        expiration.set(Calendar.HOUR, 23);
        expiration.set(Calendar.MINUTE, 59);
        expiration.set(Calendar.SECOND, 59);
        return expiration;
    }
}
