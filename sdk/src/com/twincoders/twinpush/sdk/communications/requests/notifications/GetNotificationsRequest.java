package com.twincoders.twinpush.sdk.communications.requests.notifications;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.DefaultRequestParam;
import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public class GetNotificationsRequest extends TwinPushRequest {

	public interface Listener extends ErrorListener {
		void onSuccess(List<PushNotification> notifications, int totalPages);
	}
	
	/* Constants */
	/* Segments */
	private final static String APPLICATIONS_SEGMENT = "apps";
	private final static String DEVICES_SEGMENT = "devices";
	private final static String SEARCH_SEGMENT = "search_notifications";
	/* Parameters */
	private final static String PAGE_KEY = "page";
	private final static String RESULTS_PER_PAGE_KEY = "per_page";
	private final static String TAGS_KEY = "tags";
	private final static String NO_TAGS_KEY = "no_tags";
	private final static String RICH_NOTIFICATION_TAG = "tp_rich";
	/* Response fields */
	private final static String RESPONSE_TOTAL_PAGES_KEY = "total_pages";
	private final static String RESPONSE_NOTIF_ARRAY_KEY = "objects";
	
	/* Properties */
	Listener listener;
	
	public GetNotificationsRequest(int page, int resultsPerPage, List<String> tags, List<String> noTags, boolean ignoreNonRichNotifications, Listener listener, String applicationId, String deviceId) {
		super();
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(APPLICATIONS_SEGMENT);
		addSegmentParam(applicationId);
		addSegmentParam(DEVICES_SEGMENT);
		addSegmentParam(deviceId);
		addSegmentParam(SEARCH_SEGMENT);
		// Parameters
		// Increase page value so first page is 1 
		addParam(PAGE_KEY, String.valueOf(page+1));
		addParam(RESULTS_PER_PAGE_KEY, String.valueOf(resultsPerPage));
		// Include rich tag when necessary
		if (ignoreNonRichNotifications) {
			if (tags == null) {
				tags = new ArrayList<String>();
			}
			tags.add(RICH_NOTIFICATION_TAG);
		}
		addParam(DefaultRequestParam.arrayParam(TAGS_KEY, tags));
		addParam(DefaultRequestParam.arrayParam(NO_TAGS_KEY, noTags));
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		int totalPages = 0;
		List<PushNotification> notifications = new ArrayList<PushNotification>();
		try {
			if (response.has(RESPONSE_TOTAL_PAGES_KEY)) {
				totalPages = response.getInt(RESPONSE_TOTAL_PAGES_KEY);
			}
			
			JSONArray notifJsonArray = response.getJSONArray(RESPONSE_NOTIF_ARRAY_KEY);
			for (int i=0; i<notifJsonArray.length(); i++) {
				JSONObject json = notifJsonArray.getJSONObject(i);
				PushNotification n = parseNotification(json);
				notifications.add(n);
			}
			getListener().onSuccess(notifications, totalPages);
		} catch (JSONException e) {
			Ln.e(e, "Error while trying to parse notifications from response");
			getListener().onError(e);
		}
	}
	
	public Listener getListener() {
		return listener;
	}

}
