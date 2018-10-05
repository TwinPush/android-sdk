package com.twincoders.twinpush.sdk.communications.requests.notifications;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetNotificationDetailsRequest extends TwinPushRequest {

	public interface Listener extends ErrorListener {
		void onSuccess(PushNotification notification);
	}
	
	/* Constants */
	/* Response fields */
	private final static String RESPONSE_RESULTS_KEY = "objects";
	
	/* Properties */
	Listener listener;
	
	public GetNotificationDetailsRequest(String appId, String deviceId, String notificationId, Listener listener) {
		super(appId, deviceId, notificationId);
		this.listener = listener;
		this.httpMethod = HttpMethod.GET;
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
