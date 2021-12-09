package com.twincoders.twinpush.sdk.services;

import android.content.Context;

import com.twincoders.twinpush.sdk.notifications.PushNotification;

public interface SilentPushReceiver {

    void onSilentPushReceived(Context context, PushNotification notification);

}