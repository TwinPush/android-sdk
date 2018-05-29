package com.twincoders.twinpush.sdk.demo.services;

import android.content.Context;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.communications.TwinPushRequestFactory;
import com.twincoders.twinpush.sdk.communications.requests.register.RegisterRequest;
import com.twincoders.twinpush.sdk.entities.RegistrationInfo;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.services.RegistrationIntentReceiver;

public class RegistrationReceiver extends RegistrationIntentReceiver {

    @Override
    public void onRegistrationIntent(final Context context, final RegistrationInfo info) {
        TwinPushRequestFactory.getSharedinstance(context).register(info, new RegisterRequest.Listener() {
            @Override
            public void onRegistrationSuccess(String deviceId, String deviceAlias) {
                TwinPushSDK.getInstance(context).onRegistrationSuccess(deviceId, info);
            }

            @Override
            public void onError(Exception exception) {
                Ln.e(exception);
            }
        });
    }
}