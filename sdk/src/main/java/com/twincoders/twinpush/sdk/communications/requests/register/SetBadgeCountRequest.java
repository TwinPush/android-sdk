package com.twincoders.twinpush.sdk.communications.requests.register;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;

import org.json.JSONObject;

public class SetBadgeCountRequest extends TwinPushRequest {

	/* Constants */
	private final static String[] SEGMENTS = new String[] { "update_badge" };
	/* Response fields */
	private final static String BADGE_COUNT_KEY = "badge";

	/* Properties */
	private DefaultListener listener;

	public SetBadgeCountRequest(String applicationId, String deviceId, int badgeCount, DefaultListener listener) {
		super(applicationId, deviceId);
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		for (String segment : SEGMENTS) {
			addSegmentParam(segment);
		}
		addParam(BADGE_COUNT_KEY, badgeCount);
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
