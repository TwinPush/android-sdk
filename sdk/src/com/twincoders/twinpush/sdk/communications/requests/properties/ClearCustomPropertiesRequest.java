package com.twincoders.twinpush.sdk.communications.requests.properties;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

public class ClearCustomPropertiesRequest extends TwinPushRequest {

	/* Constants */
	/* Segments */
	private final static String APPLICATIONS_SEGMENT = "apps";
	private final static String DEVICES_SEGMENT = "devices";
	private final static String ACTION_SEGMENT = "clear_custom_properties";
	
	/* Properties */
	DefaultListener listener;
	
	public ClearCustomPropertiesRequest(DefaultListener listener, String applicationId, String deviceId) {
		super();
		this.sequential = true;
		this.listener = listener;
		this.httpMethod = HttpMethod.DELETE;
		// Segments
		addSegmentParam(APPLICATIONS_SEGMENT);
		addSegmentParam(applicationId);
		addSegmentParam(DEVICES_SEGMENT);
		addSegmentParam(deviceId);
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
