package com.twincoders.twinpush.sdk.communications.requests.statistics;

import java.util.Date;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public class OpenNotificationRequest extends TwinPushRequest {
	
	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "open_notification";
	/* Parameters */
	private final static String DEVICE_ID_KEY = "device_id";
	private final static String NOTIFICATION_ID_KEY = "notification_id";
	private final static String OPEN_TIME_KEY = "open_at";
	
	/* Properties */
	DefaultListener listener;
	
	public OpenNotificationRequest(PushNotification notification, DefaultListener listener, String deviceId) {
		super();
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(ACTION_SEGMENT);
		// Parameters
		addParam(DEVICE_ID_KEY, deviceId);
		addParam(OPEN_TIME_KEY, new Date().getTime()/1000);
		addParam(NOTIFICATION_ID_KEY, notification.getId());
	}

	@Override
	public ErrorListener getListener() {
		return listener;
	}

	@Override
	protected void onSuccess(JSONObject response) {
		listener.onSuccess();
	}

}
