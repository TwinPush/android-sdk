package com.twincoders.twinpush.sdk.services;

import android.content.Context;

import com.twincoders.twinpush.sdk.notifications.PushNotification;

public interface SilentPushReceiver {

    void onSilentNotificationReceived(Context context, PushNotification notification);

}