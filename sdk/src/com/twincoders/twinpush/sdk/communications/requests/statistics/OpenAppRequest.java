package com.twincoders.twinpush.sdk.communications.requests.statistics;

import java.util.Date;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

public class OpenAppRequest extends TwinPushRequest {
	
	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "open_app";
	/* Parameters */
	private final static String DEVICE_ID_KEY = "id";
	private final static String OPEN_TIME_KEY = "open_at";
	
	/* Properties */
	DefaultListener listener;
	
	public OpenAppRequest(DefaultListener listener, String deviceId) {
		super();
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(ACTION_SEGMENT);
		// Parameters
		addParam(DEVICE_ID_KEY, deviceId);
		addParam(OPEN_TIME_KEY, new Date().getTime()/1000);
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