package com.twincoders.twinpush.sdk.services;


import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;

/**
 * Base class to handle Firebase Instance ID token refresh events and notify TwinPush server when necessary
 */

public class TwinPushInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());

        Ln.d("TwinPush Intent Service called");
        // Get updated InstanceID token.
        String refreshedToken = twinPush.getFirebaseInstanceIdToken();
        Ln.d("FCM Token created: " + refreshedToken);

        // Refresh register if needed
        if (twinPush.isDeviceRegistered()) {
            twinPush.register();
        }
    }

    @Override
    public void handleIntent(Intent intent) {
        // Init TwinPush FirebaseApp
        TwinPushSDK.getInstance(getApplicationContext()).getFirebaseApp();
        super.handleIntent(intent);
    }
}