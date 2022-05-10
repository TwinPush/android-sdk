package com.twincoders.twinpush.sdk.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.twincoders.twinpush.sdk.R;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.logging.Strings;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DefaultNotificationService {

    public final static String EXTRA_NOTIFICATION_TITLE = "title";
    public final static String EXTRA_NOTIFICATION_MESSAGE = "message";
    public final static String EXTRA_NOTIFICATION_ID = "tp_id";
    public final static String EXTRA_NOTIFICATION_CUSTOM = "custom";
    public final static String EXTRA_NOTIFICATION_TAGS = "tags";
    public final static String EXTRA_NOTIFICATION_RICH_URL = "tp_rich_url";

    public DefaultNotificationService() {}

    /**
     * Method that displays the obtained message in the Android notifications center
     * @param context Application center
     * @param notification Notification to be displayed
     * @param contentIntend Content intent for the given notification, that will be launched when user clicks notification
     */
    public void displayNotification(@NonNull Context context, @NonNull PushNotification notification, @NonNull PendingIntent contentIntend) {
        String title = notification.getTitle();
        // It title is empty, display application name
        if (title == null || title.trim().length() == 0) {
            int stringId = context.getApplicationInfo().labelRes;
            title = context.getString(stringId);
        }

        String channelId = notification.getCustomProperties().get(NotificationIntentService.PROPERTY_CHANNEL_ID);
        if (Strings.isEmpty(channelId)) channelId = context.getString(R.string.twinPush_default_channel_id);
        if (channelId != null) {
            // Create notification
            Notification push = new NotificationCompat.Builder(context, channelId)
                    .setContentTitle(title)
                    .setContentText(notification.getMessage())
                    .setTicker(notification.getMessage())
                    .setSmallIcon(R.drawable.ic_tp_notification)
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntend)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                    .build();

            NotificationManagerCompat.from(context).notify(notification.getId().hashCode(), push);
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
    public PendingIntent getContentIntent(Context context, PushNotification notification) {
        // Prepare the intent which should be launched on notification action
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.setAction(NotificationIntentService.ON_NOTIFICATION_OPENED_ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(NotificationIntentService.EXTRA_NOTIFICATION, notification);
        // Prepare the pending intent
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE :
                PendingIntent.FLAG_CANCEL_CURRENT;
        return PendingIntent.getActivity(context, notification.getId().hashCode(), intent, flags);
    }

    /**
     * Delivers a broadcast intent to notify that a silent notification has been received to the
     * client application
     * @param context Application context
     * @param notification PushNotification object with the information of the received message
     */
    public void onSilentPushReceived(Context context, PushNotification notification) {
        SilentPushReceiver silentPushReceiver = TwinPushSDK.getInstance(context).getSilentReceiver();
        if (silentPushReceiver != null) {
            Ln.d("Silent push arrived. Notifying %s...", silentPushReceiver.getClass().getName());
            silentPushReceiver.onSilentPushReceived(context, notification);
        } else {
            Ln.w("Silent push arrived, but no receiver set.");
        }
    }

    /**
     * Creates an instance of a PushNotification object with the info contained in the message intent
     * @param data Bundle containing the push notification info
     * @return PushNotification object retrieved from message
     */
    public PushNotification getNotification(Map<String, String> data) {
        // Extract info from message intent
        String notificationId = data.get(EXTRA_NOTIFICATION_ID);
        String title = data.get(EXTRA_NOTIFICATION_TITLE);
        String message = data.get(EXTRA_NOTIFICATION_MESSAGE);
        String richURL = data.get(EXTRA_NOTIFICATION_RICH_URL);
        Date date = new Date();
        Map<String, String> customProperties = getCustomPropertiesMap(data.get(EXTRA_NOTIFICATION_CUSTOM));
        List<String> tagArray = getTagArray(data.get(EXTRA_NOTIFICATION_TAGS));

        PushNotification notification = new PushNotification();
        notification.setId(notificationId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setDate(date);
        notification.setRichURL(richURL);
        notification.setCustomProperties(customProperties);
        notification.setTags(tagArray);

        return notification;
    }

    public Map<String, String> getCustomPropertiesMap(String custom) {
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

    public List<String> getTagArray(String jsonString) {
        List<String> result = new ArrayList<>();
        try {
            if (jsonString != null) {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    result.add(jsonArray.getString(i));
                }
            }
        } catch (Exception e) {
            Ln.e(e, "Error while trying to parse JSON object");
        }
        return result;
    }

}
