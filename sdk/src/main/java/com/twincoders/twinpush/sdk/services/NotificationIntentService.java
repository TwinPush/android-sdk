package com.twincoders.twinpush.sdk.services;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import java.util.Map;

public class NotificationIntentService extends FirebaseMessagingService implements PushReceiverService {

	public final static String ON_NOTIFICATION_OPENED_ACTION = "con.twincoders.twinpush.sdk.PUSH_NOTIFICATION_OPENED";
	public final static String EXTRA_NOTIFICATION = "notification";

	public final static String PROPERTY_CHANNEL_ID = "tp_channel_id";

	protected final DefaultNotificationService defaultService = new DefaultNotificationService();
	
    public NotificationIntentService() {
        super();
    }

	@Override
	public void onMessageReceived(RemoteMessage message){
    	Ln.i("Received message");

		// Check if message contains a notification payload.
		if (message.getNotification() != null) {
			super.onMessageReceived(message);
		} else {
			TwinPushSDK twinpush = TwinPushSDK.getInstance(this);
            // Ensure default channel creation to avoid issues on recently updated Android 8 devices
			twinpush.createNotificationChannel();
            // Obtain Push Notification object from message data
			PushNotification notification = getNotification(message.getData());
			// Send push notification acknowledgement if enabled
			if (twinpush.isPushAckEnabled() && NotificationManagerCompat.from(getBaseContext()).areNotificationsEnabled())
				twinpush.onNotificationReceived(notification);
			// Display Notification if not silent
			if (notification.isSilent()) {
				onSilentNotificationReceived(getBaseContext(), notification);
			} else {
				displayNotification(getBaseContext(), notification);
			}
		}
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Ln.d("TwinPush on new token called");

        TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());

        // Get updated InstanceID token.
        Ln.d("FCM Token created: " + s);

        // Refresh registration if needed
        if (twinPush.isDeviceRegistered()) {
            twinPush.register();
        }
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

	@Override
	public void onSilentNotificationReceived(Context context, PushNotification notification) {
    	defaultService.onSilentNotificationReceived(context, notification);
	}
}