package com.twincoders.twinpush.sdk.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.twincoders.twinpush.sdk.entities.RegistrationInfo;
import com.twincoders.twinpush.sdk.logging.Ln;

public abstract class RegistrationIntentReceiver extends BroadcastReceiver {

    public final static String REGISTER_REQUEST_INTENT = "com.twinpush.ON_REGISTER_REQUEST";
    private final static String REGISTER_INFO_EXTRA = "REGISTER_INFO_EXTRA";

    @Override
    public void onReceive(Context context, Intent intent) {
        Ln.i("Registration intent received");
        RegistrationInfo info = getRegistrationInfoFromIntent(intent);
        if (info != null) {
            onRegistrationIntent(context, info);
        } else {
            Ln.e("Can not proceed with external registration: registration info is null");
        }
    }

    public abstract void onRegistrationIntent(Context context, RegistrationInfo info);

    /**
     * Obtains the registration info from a given intent extra
     */
    private RegistrationInfo getRegistrationInfoFromIntent(@NonNull Intent intent) {
        RegistrationInfo info = null;
        if (intent.hasExtra(REGISTER_INFO_EXTRA)) {
            try {
                info = (RegistrationInfo) intent.getSerializableExtra(REGISTER_INFO_EXTRA);
            } catch (Exception ex) {
                Ln.e(ex, "Error when trying to obtain registration info from intent");
            }
        }
        return info;
    }

    /**
     * Launchs a broadcast intent for the registration request including the registration info as extra
     * @param context Current application context
     * @param info Container with the information for the registration
     */
    public static void launchExternalRegistrationIntent(@NonNull Context context, @NonNull RegistrationInfo info) {
        Ln.d("Starting intent with ACTION '%s'", REGISTER_REQUEST_INTENT);
        // Send a broadcast intent
        Intent registrationIntent = new Intent(REGISTER_REQUEST_INTENT);
        registrationIntent.putExtra(REGISTER_INFO_EXTRA, info);
        LocalBroadcastManager.getInstance(context).sendBroadcast(registrationIntent);
    }



}
