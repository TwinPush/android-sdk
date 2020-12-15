package com.twincoders.twinpush.sdk.services;

import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationManagerCompat;

import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import java.util.Map;

public class TpHmsMessageService extends HmsMessageService implements PushReceiverService {

    protected final DefaultNotificationService defaultService = new DefaultNotificationService();

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Ln.i("Received message");

        // Check if message contains a notification payload.
        if (message.getDataOfMap().containsKey(DefaultNotificationService.EXTRA_NOTIFICATION_MESSAGE)) {
            TwinPushSDK twinpush = TwinPushSDK.getInstance(this);
            // Ensure default channel creation to avoid issues on recently updated Android 8 devices
            twinpush.createNotificationChannel();
            // Obtain Push Notification object from message data
            PushNotification notification = getNotification(message.getDataOfMap());
            // Send push notification acknowledgement if enabled
            if (twinpush.isPushAckEnabled() && NotificationManagerCompat.from(getBaseContext()).areNotificationsEnabled())
                twinpush.onNotificationReceived(notification);
            // Display Notification
            displayNotification(getBaseContext(), notification);
        } else {
            Ln.i("Attribute 'message' not found in payload, calling parent method");
            super.onMessageReceived(message);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Ln.d("TwinPush Huawei HMS on new token called");
        TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());
        // Get updated InstanceID token.
        Ln.d("Huawei HMS Token created: " + s);
        // Refresh register if needed
        if (twinPush.isDeviceRegistered()) {
            twinPush.register();
        }
    }

    @Override
    public void onTokenError(Exception e) {
        Ln.e(e, "Error obtaining Huawei Push Token");
        super.onTokenError(e);
    }


    // Push receiver service implementation

    @Override
    public void displayNotification(Context context, PushNotification notification) {
        defaultService.displayNotification(context, notification, getContentIntent(context, notification));
    }

    @Override
    public PendingIntent getContentIntent(Context context, PushNotification notification) {
        return defaultService.getContentIntent(context, notification);
    }

    @Override
    public PushNotification getNotification(Map<String, String> data) {
        return defaultService.getNotification(data);
    }
}
