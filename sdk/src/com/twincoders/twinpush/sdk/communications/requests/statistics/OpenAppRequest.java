package com.twincoders.twinpush.sdk.communications.requests.statistics;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

public class OpenAppRequest extends TwinPushRequest {
	
	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "open_app";
	
	/* Properties */
	DefaultListener listener;
	
	public OpenAppRequest(String appId, String deviceId, DefaultListener listener) {
		super(appId, deviceId);
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