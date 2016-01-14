package com.yellowpineapple.wakup.sdk.models;

import android.content.Context;
import android.location.Location;

import com.yellowpineapple.wakup.sdk.R;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by agutierrez on 02/02/15.
 */
public class Offer implements Serializable {

    int id;
    boolean isOnline;
    CompanyDetail company;
    Category category;
    String description;
    String shortDescription;
    String shortOffer;
    String link;
    Date expirationDate;
    RemoteImage image;
    RemoteImage thumbnail;
    Store store;

    public boolean hasLocation() {
        return store != null && store.getLocation() != null;
    }

    private static int KM_LIMIT = 1000;

    public CharSequence getHumanizedDistance(Context context, Location currentLocation) {
        CharSequence distanceText;
        distanceText = context.getText(R.string.wk_offer_distance_undefined);

        if (currentLocation != null) {
            if (hasLocation()) {
                int distance = store.getDistance(currentLocation);
                if (distance != Store.LOCATION_INVALID) {
                    if (distance < KM_LIMIT) {
                        distanceText = String.format(context.getText(R.string.wk_offer_distance_x_meters).toString(), distance);
                    } else {
                        distanceText = String.format(context.getText(R.string.wk_offer_distance_x_km).toString(), (distance / 1000f));
                    }
                }
            } else if (isOnline()) {
                distanceText = context.getText(R.string.wk_offer_online);
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
                expirationText = context.getText(R.string.wk_offer_expired);
            } else {
                long diff = expiration.getTimeInMillis() - today.getTimeInMillis();
                int dayDiff = Math.round(diff / (24 * 60 * 60 * 1000));
                switch (dayDiff) {
                    case 0: {
                        expirationText = context.getText(R.string.wk_offer_expires_today);
                        break;
                    }
                    case 1: {
                        expirationText = context.getText(R.string.wk_offer_expires_tomorrow);
                        break;
                    }
                    default: {
                        int monthDiff = dayDiff / 30;
                        if (monthDiff == 0) {
                            expirationText = String.format(context.getText(R.string.wk_offer_expires_in_x_days).toString(), dayDiff);
                        } else if (monthDiff == 1) {
                            expirationText = context.getText(R.string.wk_offer_expires_in_1_month);
                        } else if (monthDiff <= 12) {
                            expirationText = String.format(context.getText(R.string.wk_offer_expires_in_x_months).toString(), monthDiff);
                        } else {
                            DateFormat df = new SimpleDateFormat(context.getText(R.string.wk_offer_expiration_date_format).toString());
                            expirationText = df.format(expirationDate);
                        }
                    }
                }
            }
        } else {
            expirationText = context.getText(R.string.wk_offer_expires_undefined);
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

    public Store getStore() {
        return store;
    }

    public int getId() {
        return id;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public CompanyDetail getCompany() {
        return company;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getShortOffer() {
        return shortOffer;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public RemoteImage getImage() {
        return image;
    }

    public RemoteImage getThumbnail() {
        return thumbnail;
    }

    public void setCompany(CompanyDetail company) {
        this.company = company;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public String getLink() {
        return link;
    }
}
