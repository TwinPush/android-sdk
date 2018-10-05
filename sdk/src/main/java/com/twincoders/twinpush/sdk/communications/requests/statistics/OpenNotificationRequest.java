package com.twincoders.twinpush.sdk.communications.requests.statistics;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

import org.json.JSONObject;

public class OpenNotificationRequest extends TwinPushRequest {
	
	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "open_notification";
	
	/* Properties */
	DefaultListener listener;
	
	public OpenNotificationRequest(String appId, String deviceId, String notificationId, DefaultListener listener) {
		super(appId, deviceId, notificationId);
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(ACTION_SEGMENT);
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
