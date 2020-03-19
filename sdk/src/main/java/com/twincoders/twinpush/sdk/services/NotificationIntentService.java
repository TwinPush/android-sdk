package com.twincoders.twinpush.sdk.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.twincoders.twinpush.sdk.R;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.logging.Strings;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NotificationIntentService extends FirebaseMessagingService {

	public final static String EXTRA_NOTIFICATION_TITLE = "title";
	public final static String EXTRA_NOTIFICATION_MESSAGE = "message";
	public final static String EXTRA_NOTIFICATION_ID = "tp_id";
	public final static String EXTRA_NOTIFICATION_CUSTOM = "custom";
	public final static String EXTRA_NOTIFICATION_RICH_URL = "tp_rich_url";
	
	public final static String ON_NOTIFICATION_OPENED_ACTION = "con.twincoders.twinpush.sdk.PUSH_NOTIFICATION_OPENED";
	public final static String EXTRA_NOTIFICATION = "notification";

	public final static String PROPERTY_CHANNEL_ID = "tp_channel_id";
	
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
			// Display Notification
			displayNotification(getBaseContext(), notification);
		}
    }

    /**
     * Method that displays the obtained message in the Android notifications center
     * @param context Application center
     * @param notification Notification to be displayed
     */
    protected void displayNotification(Context context, PushNotification notification) {
    	PendingIntent pendingIntent = getContentIntent(context, notification);
    	
		String title = notification.getTitle();
		// It title is empty, display application name
		if (title == null || title.trim().length() == 0) {
			int stringId = context.getApplicationInfo().labelRes;
			title = context.getString(stringId);
		}

		String channelId = notification.getCustomProperties().get(PROPERTY_CHANNEL_ID);
        if (Strings.isEmpty(channelId)) channelId = getString(R.string.twinPush_default_channel_id);
        if (channelId != null) {
            // Create notification
            Notification push = new NotificationCompat.Builder(context, channelId)
                    .setContentTitle(title)
                    .setContentText(notification.getMessage())
                    .setTicker(notification.getMessage())
                    .setSmallIcon(R.drawable.ic_tp_notification)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                    .build();

            NotificationManagerCompat.from(this).notify(notification.getId().hashCode(), push);
        } else {
            Ln.e("ERROR: Notification not displayed. Notification channel can not be null");
        }
	}
    
    /**
     * Obtains the content intent for a given notification. This intent will be launched when the user clicks on the notification 
     * @param context Application context
     * @param notification PushNotification object with the information of the received message
     * @return Content intent for the given notification
     */
    protected PendingIntent getContentIntent(Context context, PushNotification notification) {
    	// Prepare the intent which should be launched on notification action
    	Intent intent = getPackageManager().getLaunchIntentForPackage(context.getPackageName());
    	intent.setAction(ON_NOTIFICATION_OPENED_ACTION);
    	intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	intent.putExtra(EXTRA_NOTIFICATION, notification);
        // Prepare the pending intent
        return PendingIntent.getActivity(context, notification.getId().hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

	/**
	 * Creates an instance of a PushNotification object with the info contained in the message intent
	 * @param data Bundle containing the push notification info
	 * @return PushNotification object retrieved from message
	 */
	protected PushNotification getNotification(Map<String, String> data) {
		// Extract info from message intent
		String notificationId = data.get(EXTRA_NOTIFICATION_ID);
		String title = data.get(EXTRA_NOTIFICATION_TITLE);
		String message = data.get(EXTRA_NOTIFICATION_MESSAGE);
		String richURL = data.get(EXTRA_NOTIFICATION_RICH_URL);
		Date date = new Date();
		Map<String, String> customProperties = getCustomPropertiesMap(data.get(EXTRA_NOTIFICATION_CUSTOM));

		PushNotification notification = new PushNotification();
		notification.setId(notificationId);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setDate(date);
		notification.setRichURL(richURL);
		notification.setCustomProperties(customProperties);

		return notification;
	}

	private Map<String, String> getCustomPropertiesMap(String custom) {
		Map<String, String> propertiesMap = new HashMap<>();
		try {
			if (custom != null) {
				JSONObject json = new JSONObject(custom);
				Iterator<?> iterator = json.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					try {
						String value = (String) json.get(key);
						propertiesMap.put(key, value);
					} catch (JSONException e) {
						Ln.e(e, "Could not find property %s on Custom properties JSON", key);
					}
				}
			}
		} catch (Exception e) {
			Ln.e(e, "Error while trying to parse JSON object");
		}
		return propertiesMap;
	}

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Ln.d("TwinPush on new token called");

        TwinPushSDK twinPush = TwinPushSDK.getInstance(getApplicationContext());

        // Get updated InstanceID token.
        Ln.d("FCM Token created: " + s);

        // Refresh register if needed
        if (twinPush.isDeviceRegistered()) {
            twinPush.register();
        }
    }
}
