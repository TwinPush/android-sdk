package com.twincoders.twinpush.sdk.notifications;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gcm.GCMBaseIntentService;
import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.util.WakeLocker;

public class TwinPushIntentService extends GCMBaseIntentService {

	/* On Register Intent */
	public final static String ON_REGISTERED_ACTION = "com.twincoders.twinpush.sdk.ON_REGISTERED";
	public final static String ON_REGISTER_ERROR_ACTION = "com.twincoders.twinpush.sdk.ON_REGISTER_ERROR";
	public final static String EXTRA_REGISTRATION_ID = "REGISTER_ID";
	public final static String EXTRA_REGISTRATION_ERROR = "REGISTER_ERROR";

	public final static String EXTRA_NOTIFICATION_TITLE = "title";
	public final static String EXTRA_NOTIFICATION_MESSAGE = "message";
	public final static String EXTRA_NOTIFICATION_ID = "tp_id";
	public final static String EXTRA_NOTIFICATION_CUSTOM = "custom";
	public final static String EXTRA_NOTIFICATION_RICH_URL = "tp_rich_url";

	public final static String ON_NOTIFICATION_OPENED_ACTION = "con.twincoders.twinpush.sdk.PUSH_NOTIFICATION_OPENED";
	public final static String EXTRA_NOTIFICATION = "notification";

	public TwinPushIntentService() {
		super();
	}

	/**
	 * Method called on device registered
	 **/
	@Override
	protected void onRegistered(Context context, String registrationId) {
		Ln.i("Device registered with id: %s", registrationId);
		Intent intent = new Intent(ON_REGISTERED_ACTION);
		intent.putExtra(EXTRA_REGISTRATION_ID, registrationId);
		context.sendBroadcast(intent);
	}

	/**
	 * Method called on device un registred
	 * */
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Ln.i("Device unregistered");
	}

	/**
	 * Method called on Receiving a new message
	 * */
	@Override
	protected void onMessage(Context context, Intent paramsIntent) {
		Ln.i("Received message");
		if (paramsIntent.hasExtra(EXTRA_NOTIFICATION_ID)) {
			PushNotification notification = getNotification(paramsIntent);
			// notifies user
			displayNotification(context, notification);
		}
	}

	/**
	 * Method called on receiving a deleted message
	 * */
	@Override
	protected void onDeletedMessages(Context context, int total) {
		Ln.i("Received deleted messages notification");
	}

	/**
	 * Method called on Error
	 * */
	@Override
	public void onError(Context context, String errorId) {
		Ln.w("Received error: %s", errorId);
		Intent intent = new Intent(ON_REGISTERED_ACTION);
		intent.putExtra(EXTRA_REGISTRATION_ERROR, errorId);
		context.sendBroadcast(intent);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		Ln.i("Received recoverable error: %s", errorId);
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Method that displays the obtained message in the Android notifications
	 * center
	 * 
	 * @param context
	 *            Application center
	 * @param notification
	 *            Notification to be displayed
	 */
	protected void displayNotification(Context context,
			PushNotification notification) {
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
				.setSmallIcon(
						TwinPushSDK.getInstance(context)
								.getNotificationSmallIcon())
				.setDefaults(
						Notification.DEFAULT_LIGHTS
								| Notification.DEFAULT_VIBRATE
								| Notification.DEFAULT_SOUND)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setStyle(
						new NotificationCompat.BigTextStyle()
								.bigText(notification.getMessage())).build();

		WakeLocker.acquire(context);

		NotificationManager manager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(notification.getId().hashCode(), push);
	}

	/**
	 * Obtains the content intent for a given notification. This intent will be
	 * launched when the user clicks on the notification
	 * 
	 * @param context
	 *            Application context
	 * @param notification
	 *            PushNotification object with the information of the received
	 *            message
	 * @return Content intent for the given notification
	 */
	protected PendingIntent getContentIntent(Context context,
			PushNotification notification) {
		// Prepare the intent which should be launched on notification action
		Intent intent = getPackageManager().getLaunchIntentForPackage(
				context.getPackageName());
		intent.setAction(ON_NOTIFICATION_OPENED_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(EXTRA_NOTIFICATION, notification);
		// Prepare the pending intent
		PendingIntent pendingIntent = PendingIntent.getActivity(context,
				notification.getId().hashCode(), intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		return pendingIntent;
	}

	/**
	 * Creates an instance of a PushNotification object with the info contained
	 * in the message intent
	 * 
	 * @param messageIntent
	 *            Intent containing the push notification info
	 * @return PushNotification object retrieved from message
	 */
	protected PushNotification getNotification(Intent messageIntent) {
		// Extract info from message intent
		String notificationId = messageIntent
				.getStringExtra(EXTRA_NOTIFICATION_ID);
		String title = messageIntent.getStringExtra(EXTRA_NOTIFICATION_TITLE);
		String message = messageIntent
				.getStringExtra(EXTRA_NOTIFICATION_MESSAGE);
		String richURL = messageIntent
				.getStringExtra(EXTRA_NOTIFICATION_RICH_URL);
		Date date = new Date();
		Map<String, String> customProperties = getCustomPropertiesMap(messageIntent);

		PushNotification notification = new PushNotification();
		notification.setId(notificationId);
		notification.setTitle(title);
		notification.setMessage(message);
		notification.setDate(date);
		notification.setRichURL(richURL);
		notification.setCustomProperties(customProperties);

		return notification;
	}

	private Map<String, String> getCustomPropertiesMap(Intent messageIntent) {
		Map<String, String> propertiesMap = new HashMap<String, String>();
		try {
			// Extract raw custom String
			String custom = messageIntent
					.getStringExtra(EXTRA_NOTIFICATION_CUSTOM);
			if (custom != null) {
				JSONObject json = new JSONObject(custom);
				Iterator<?> iter = json.keys();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					try {
						String value = (String) json.get(key);
						propertiesMap.put(key, value);
					} catch (JSONException e) {
						Ln.e(e,
								"Could not find property %1$s on Custom properties JSON");
					}
				}
			}
		} catch (Exception e) {
			Ln.e(e, "Error while trying to parse JSON object");
		}
		return propertiesMap;
	}

	@Override
	protected String[] getSenderIds(Context context) {
		String senderId = TwinPushSDK.getInstance(context).getGCMSenderId();
		return new String[] { senderId };
	}
}
