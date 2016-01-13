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

    public void launch(Options options) {
        persistence.setOptions(options);
        OffersActivity.intent(context).start();
    }

    public Options getOptions() {
        return persistence.getOptions();
    }

    public static class Options {

        /* Wakup Client API Key */
        public String apiKey = null;

        // Action bar
        public int actionBarLogo = R.drawable.ic_cast_dark;

        public Options() {
            super();
        }

    }

}
