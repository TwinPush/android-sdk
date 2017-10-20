package com.twincoders.twinpush.sdk.services;


import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.twincoders.twinpush.sdk.DefaultTwinPushSDK;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;

/**
 * Base class to handle Firebase Instance ID token refresh events and notify TwinPush server when necessary
 */

public class TwinPushInstanceIdService extends FirebaseInstanceIdService {

    public final static String REGISTRATION_COMPLETE = "REGISTRATION_COMPLETE";
    public final static String EXTRA_PUSH_TOKEN = "PUSH_TOKEN";

    @Override
    public void onTokenRefresh() {
        TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());

        Ln.d("TwinPush Intent Service called");
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance(twinPush.getFirebaseApp()).getToken();
        Ln.d("FCM Token created: " + refreshedToken);

        // Refresh register if needed
        if (twinPush.getDeviceId() != null) {
            twinPush.register();
        }
        // Notify that registration has completed through a broadcast intent
        Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        if (refreshedToken != null) registrationComplete.putExtra(EXTRA_PUSH_TOKEN, refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    @Override
    public void handleIntent(Intent intent) {
        // Init TwinPush FirebaseApp
        TwinPushSDK.getInstance(getApplicationContext()).getFirebaseApp();
        super.handleIntent(intent);
    }
}