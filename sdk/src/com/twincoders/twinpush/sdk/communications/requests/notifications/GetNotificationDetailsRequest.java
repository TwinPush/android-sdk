package com.twincoders.twinpush.sdk.communications.requests.notifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public class GetNotificationDetailsRequest extends TwinPushRequest {

	public interface Listener extends ErrorListener {
		void onSuccess(PushNotification notification);
	}
	
	/* Constants */
	/* Segments */
	private final static String APPLICATIONS_SEGMENT = "apps";
	private final static String NOTIFICATIONS_SEGMENT = "notifications";
	/* Response fields */
	private final static String RESPONSE_RESULTS_KEY = "objects";
	
	/* Properties */
	Listener listener;
	
	public GetNotificationDetailsRequest(String notificationId, Listener listener, String applicationId) {
		super();
		this.listener = listener;
		this.httpMethod = HttpMethod.GET;
		// Segments
		addSegmentParam(APPLICATIONS_SEGMENT);
		addSegmentParam(applicationId);
		addSegmentParam(NOTIFICATIONS_SEGMENT);
		addSegmentParam(notificationId);
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		try {
			JSONArray notifJsonArray = response.getJSONArray(RESPONSE_RESULTS_KEY);
			if (notifJsonArray.length() > 0) {
				JSONObject json = notifJsonArray.getJSONObject(0);
				PushNotification n = parseNotification(json);
				getListener().onSuccess(n);
			} else {
				getListener().onError(new Exception("Notification not found"));
			}
			
		} catch (JSONException e) {
			Ln.e(e, "Error while trying to parse notifications from response");
			getListener().onError(e);
		}
	}
	
	public Listener getListener() {
		return listener;
	}

}
