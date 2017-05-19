package com.twincoders.twinpush.sdk.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NotificationIntentService extends GcmListenerService {

	public final static String EXTRA_NOTIFICATION_TITLE = "title";
	public final static String EXTRA_NOTIFICATION_MESSAGE = "message";
	public final static String EXTRA_NOTIFICATION_ID = "tp_id";
	public final static String EXTRA_NOTIFICATION_CUSTOM = "custom";
	public final static String EXTRA_NOTIFICATION_RICH_URL = "tp_rich_url";
	
	public final static String ON_NOTIFICATION_OPENED_ACTION = "con.twincoders.twinpush.sdk.PUSH_NOTIFICATION_OPENED";
	public final static String EXTRA_NOTIFICATION = "notification";
	
    public NotificationIntentService() {
        super();
    }

	public void onMessageReceived(String from, Bundle data) {
    	Ln.i("Received message");
        
        PushNotification notification = getNotification(data);
        
        // notifies user
        displayNotification(getBaseContext(), notification);
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
		
		// Create notification
		Notification push = new NotificationCompat.Builder(context)
        .setContentTitle(title)
        .setContentText(notification.getMessage())
        .setTicker(notification.getMessage())
        .setSmallIcon(TwinPushSDK.getInstance(context).getNotificationSmallIcon())
        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
		.setContentIntent(pendingIntent)
		.setAutoCancel(true)
		.setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
		.build();
		
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(notification.getId().hashCode(), push);
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
    protected PushNotification getNotification(Bundle data) {
    	// Extract info from message intent
    	String notificationId = data.getString(EXTRA_NOTIFICATION_ID);
    	String title = data.getString(EXTRA_NOTIFICATION_TITLE);
		String message = data.getString(EXTRA_NOTIFICATION_MESSAGE);
		String richURL = data.getString(EXTRA_NOTIFICATION_RICH_URL);
		Date date = new Date();
		Map<String, String> customProperties = getCustomPropertiesMap(data);
		
		PushNotification notification = new PushNotification();
		notification.setId(notificationId);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setDate(date);
		notification.setRichURL(richURL);
		notification.setCustomProperties(customProperties);
		
		return notification;
    }
    
    private Map<String, String> getCustomPropertiesMap(Bundle data) {
    	Map<String, String> propertiesMap = new HashMap<>();
    	try {
    		// Extract raw custom String
    		String custom = data.getString(EXTRA_NOTIFICATION_CUSTOM);
    		if (custom != null) {
	    		JSONObject json = new JSONObject(custom);
	    		Iterator<?> iter = json.keys();
	    		while (iter.hasNext()) {
	    			String key = (String) iter.next();
	    			try {
	    				String value = (String) json.get(key);
	    				propertiesMap.put(key, value);
	    			} catch (JSONException e) {
	    				Ln.e(e, "Could not find property %1$s on Custom properties JSON");
	    			}
	    		}
    		}
    	} catch (Exception e) {
    		Ln.e(e, "Error while trying to parse JSON object");
    	}
    	return propertiesMap;
    }
}
