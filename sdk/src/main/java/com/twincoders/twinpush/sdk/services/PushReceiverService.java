package com.twincoders.twinpush.sdk.services;

import android.app.PendingIntent;
import android.content.Context;

import com.twincoders.twinpush.sdk.notifications.PushNotification;

import java.util.Map;

public interface PushReceiverService {

    /**
     * Method that displays the obtained message in the Android notifications center
     * @param context Application center
     * @param notification Notification to be displayed
     */
    void displayNotification(Context context, PushNotification notification);

    /**
     * Obtains the content intent for a given notification. This intent will be launched when the user clicks on the notification
     * @param context Application context
     * @param notification PushNotification object with the information of the received message
     * @return Content intent for the given notification
     */
    PendingIntent getContentIntent(Context context, PushNotification notification);

    /**
     * Creates an instance of a PushNotification object with the info contained in the message intent
     * @param data Bundle containing the push notification info
     * @return PushNotification object retrieved from message
     */
    PushNotification getNotification(Map<String, String> data);

    /**
     * Delivers a broadcast intent to notify that a silent notification has been received to the
     * client application
     * @param context Application context
     * @param notification PushNotification object with the information of the received message
     */
    void onSilentPushReceived(Context context, PushNotification notification);

}