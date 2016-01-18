package com.yellowpineapple.wakup.sdk;

import android.content.Context;

import com.yellowpineapple.wakup.sdk.activities.OffersActivity;
import com.yellowpineapple.wakup.sdk.utils.PersistenceHandler;

/**
 * Created by agutierrez on 13/1/16.
 */
public class Wakup {

    private static Wakup sharedInstance = null;
    private Context context;

    private PersistenceHandler persistence;

    private static final String BIG_OFFER_URL = "https://app.wakup.net/offers/highlighted/%s";

    private Wakup(Context context) {
        super();
        this.context = context;
        this.persistence = PersistenceHandler.getSharedInstance(context);
    }

    public static Wakup instance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new Wakup(context);
        }
        return sharedInstance;
    }

    public void launch(WakupOptions options) {
        persistence.setOptions(options);
        OffersActivity.intent(context).start();
    }

    public WakupOptions getOptions() {
        return persistence.getOptions();
    }

    public String getBigOffer() {
        return String.format(BIG_OFFER_URL, getOptions().getApiKey());
    }

}
