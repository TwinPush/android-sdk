package com.twincoders.twinpush.sdk.communications.requests.notifications;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

import org.json.JSONObject;

public class DeleteInboxNotificationRequest extends TwinPushRequest {

	/* Properties */
	DefaultListener listener;

	public DeleteInboxNotificationRequest(String appId, String deviceId, String notificationId, DefaultListener listener) {
		super(appId, deviceId, notificationId);
		this.listener = listener;
		this.httpMethod = HttpMethod.DELETE;
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		if (getListener() != null) {
			getListener().onSuccess();
		}
	}
	
	public DefaultListener getListener() {
		return listener;
	}

}
