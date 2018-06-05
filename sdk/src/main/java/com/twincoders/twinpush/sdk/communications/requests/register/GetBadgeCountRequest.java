package com.twincoders.twinpush.sdk.communications.requests.register;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.logging.Ln;

import org.json.JSONException;
import org.json.JSONObject;

public class GetBadgeCountRequest extends TwinPushRequest {

	public interface Listener extends ErrorListener {
		void onSuccess(int badgeCount);
	}

	/* Constants */
	private final static String[] SEGMENTS = new String[] { "badge" };
	/* Response fields */
	private final static String BADGE_COUNT_KEY = "badge";

	/* Properties */
	private Listener listener;

	public GetBadgeCountRequest(String applicationId, String deviceId, Listener listener) {
		super(applicationId, deviceId);
		this.listener = listener;
		this.httpMethod = HttpMethod.GET;
		// Segments
		for (String segment : SEGMENTS) {
			addSegmentParam(segment);
		}
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		try {
            int badgeCount = response.getInt(BADGE_COUNT_KEY);
            getListener().onSuccess(badgeCount);
		} catch (JSONException e) {
			Ln.e(e, "Error while trying to parse badge count from response");
			getListener().onError(e);
		}
	}
	
	public Listener getListener() {
		return listener;
	}

}
