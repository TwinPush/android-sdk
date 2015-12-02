package com.twincoders.twinpush.sdk.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;

public class RegistrationIntentService extends IntentService {

    public final static String REGISTRATION_COMPLETE = "REGISTRATION_COMPLETE";
    public final static String EXTRA_REGISTRATION_ERROR = "REGISTRATION_ERROR";
    public final static String EXTRA_PUSH_TOKEN = "PUSH_TOKEN";


    public RegistrationIntentService() {
        super(RegistrationIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());
        String pushToken = null;
        String error = null;

        try {
            Ln.d("Obtaining GCM Push Token");
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(twinPush.getGcmProjectNumber(),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Ln.d("GCM Registration Push Token: %s", token);

            pushToken = token;
        } catch (Exception e) {
            Ln.e(e, "Failed to complete token refresh");
            error = "Failed to complete token refresh: " + e.getLocalizedMessage();
        }
        // Notify that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        if (pushToken != null) registrationComplete.putExtra(EXTRA_PUSH_TOKEN, pushToken);
        if (error != null) registrationComplete.putExtra(EXTRA_REGISTRATION_ERROR, error);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}