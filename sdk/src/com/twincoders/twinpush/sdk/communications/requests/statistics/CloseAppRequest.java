package com.twincoders.twinpush.sdk.communications.requests.statistics;

import java.util.Date;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

public class CloseAppRequest extends TwinPushRequest {

	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "close_app";
	/* Parameters */
	private final static String DEVICE_ID_KEY = "id";
	private final static String CLOSE_TIME_KEY = "closed_at";
	
	/* Properties */
	DefaultListener listener;
	
	public CloseAppRequest(DefaultListener listener, String deviceId) {
		super();
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(ACTION_SEGMENT);
		// Parameters
		addParam(DEVICE_ID_KEY, deviceId);
		addParam(CLOSE_TIME_KEY, new Date().getTime()/1000);
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