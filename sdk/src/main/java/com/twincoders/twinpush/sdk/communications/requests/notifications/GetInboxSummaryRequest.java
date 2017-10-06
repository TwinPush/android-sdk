package com.twincoders.twinpush.sdk.communications.requests.notifications;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.entities.InboxSummary;
import com.twincoders.twinpush.sdk.logging.Ln;

import org.json.JSONException;
import org.json.JSONObject;

public class GetInboxSummaryRequest extends TwinPushRequest {

	public interface Listener extends ErrorListener {
		void onSuccess(InboxSummary inboxSummary);
	}

	/* Constants */
	private final static String[] SEGMENTS = new String[] { "inbox_summary" };
	/* Response fields */
	private final static String TOTAL_COUNT_KEY = "total_count";
	private final static String UNOPENED_COUNT_KEY = "unopened_count";

	/* Properties */
	private Listener listener;

	public GetInboxSummaryRequest(String applicationId, String deviceId, Listener listener) {
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
            InboxSummary summary = new InboxSummary();
            summary.setTotalCount(response.getInt(TOTAL_COUNT_KEY));
            summary.setUnopenedCount(response.getInt(UNOPENED_COUNT_KEY));
			getListener().onSuccess(summary);
		} catch (JSONException e) {
			Ln.e(e, "Error while trying to parse inbox summary from response");
			getListener().onError(e);
		}
	}
	
	public Listener getListener() {
		return listener;
	}

}
