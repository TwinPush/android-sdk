package com.twincoders.twinpush.sdk.controllers;

import com.twincoders.twinpush.sdk.notifications.PushNotification;

public interface LegacyNotificationItemView {

    public interface Listener {
        void onNotificationSelected(PushNotification notification);
        void onNotificationLongClicked(PushNotification notification);
    }

    void setListener(Listener listener);
    void setNotification(PushNotification notification);

}