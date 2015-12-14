package com.yellowpineapple.wakup.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.yellowpineapple.wakup.models.Offer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by agutierrez on 19/02/15.
 */
public class PersistenceHandler {

    private static PersistenceHandler INSTANCE = null;
    private static final String PREFS_NAME = "101OffersPref";
    private static final String KEY_USER_OFFERS = "KEY_USER_OFFERS";

    private SharedPreferences preferences = null;
    private List<String> userOffers = null;

    Date savedOffersUpdatedAt = new Date();

    Context context;

    private PersistenceHandler(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public static PersistenceHandler getSharedInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PersistenceHandler(context);
        }
        return INSTANCE;
    }

    private SharedPreferences getPreferences() {
        return preferences;
    }

    public List<String> getUserOffers() {
        if (userOffers == null) {
            userOffers = new ArrayList<>(getPreferences().getStringSet(KEY_USER_OFFERS, new HashSet<String>()));
        }
        return userOffers;
    }

    public boolean isSavedOffer(Offer offer) {
        return getUserOffers().contains(getOfferKey(offer));
    }

    public void saveOffer(Offer offer) {
        String offerKey = getOfferKey(offer);
        userOffers.add(offerKey);
        storeUserOffers();
    }

    public void removeSavedOffer(Offer offer) {
        String offerKey = getOfferKey(offer);
        if (userOffers.contains(offerKey)) {
            userOffers.remove(offerKey);
            storeUserOffers();
        }
    }

    private String getOfferKey(Offer offer) {
        return String.valueOf(offer.getId());
    }

    private void storeUserOffers() {
        Set<String> offerKeys = new HashSet<>();
        offerKeys.addAll(getUserOffers());
        getPreferences().edit().putStringSet(KEY_USER_OFFERS, offerKeys).commit();
        savedOffersUpdatedAt = new Date();
    }

    public boolean userOffersChanged(Date dateSince) {
        return savedOffersUpdatedAt != null && savedOffersUpdatedAt.after(dateSince);
    }
}
