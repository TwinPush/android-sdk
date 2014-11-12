package com.twincoders.twinpush.sdk.communications.requests.properties;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

public class ClearCustomPropertiesRequest extends TwinPushRequest {

	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "clear_custom_properties";
	
	/* Properties */
	DefaultListener listener;
	
	public ClearCustomPropertiesRequest(String applicationId, String deviceId, DefaultListener listener) {
		super(applicationId, deviceId);
		this.sequential = true;
		this.listener = listener;
		this.httpMethod = HttpMethod.DELETE;
		// Segments
		addSegmentParam(ACTION_SEGMENT);
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		getListener().onSuccess();
	}
	
	public DefaultListener getListener() {
		return listener;
	}
	
}
