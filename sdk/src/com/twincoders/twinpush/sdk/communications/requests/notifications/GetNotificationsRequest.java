package com.twincoders.twinpush.sdk.communications.requests.notifications;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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
	private final static String RESPONSE_NOTIF_ID_KEY = "id";
	private final static String RESPONSE_NOTIF_TITLE_KEY = "title";
	private final static String RESPONSE_NOTIF_MESSAGE_KEY = "alert";
	private final static String RESPONSE_NOTIF_TAGS_KEY = "tags";
	private final static String RESPONSE_NOTIF_SOUND_KEY = "sound";
	private final static String RESPONSE_NOTIF_RICH_URL_KEY = "tp_rich_url";
	private final static String RESPONSE_NOTIF_CUSTOM_PROPERTIES_KEY = "custom_properties";
	private final static String RESPONSE_NOTIF_DATE = "last_sent_at";
	private final static String RESPONSE_NOTIF_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss 'UTC'";
	
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
				PushNotification n = new PushNotification();
				n.setId(json.getString(RESPONSE_NOTIF_ID_KEY));
				n.setTitle(json.getString(RESPONSE_NOTIF_TITLE_KEY));
				n.setMessage(json.getString(RESPONSE_NOTIF_MESSAGE_KEY));
				n.setSound(json.getString(RESPONSE_NOTIF_SOUND_KEY));
				n.setRichURL(json.getString(RESPONSE_NOTIF_RICH_URL_KEY));
				n.setCustomProperties(getCustomPropertiesMap(json.getJSONObject(RESPONSE_NOTIF_CUSTOM_PROPERTIES_KEY)));
				if (!json.isNull(RESPONSE_NOTIF_TAGS_KEY)) {
					n.setTags(getTags(json.getJSONArray(RESPONSE_NOTIF_TAGS_KEY)));
				}
				// Parse date
				SimpleDateFormat dateFormat = new SimpleDateFormat(RESPONSE_NOTIF_DATE_FORMAT, Locale.UK);
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date date;
				try {
					date = dateFormat.parse(json.getString(RESPONSE_NOTIF_DATE));
				} catch (ParseException e) {
					Ln.e(e, "Error while trying to parse notification date");
					date = new Date();
					Ln.i("Current format: %s", dateFormat.format(date));
				}
				n.setDate(date);
				notifications.add(n);
			}
			getListener().onSuccess(notifications, totalPages);
		} catch (JSONException e) {
			Ln.e(e, "Error while trying to parse notifications from response");
			getListener().onError(e);
		}
	}
	
	protected Map<String, String> getCustomPropertiesMap(JSONObject json) {
		Map<String, String> propertiesMap = new HashMap<String, String>();
		Iterator<?> iter = json.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			try {
				if (!json.isNull(key)) {
					String value = (String) json.get(key);
					propertiesMap.put(key, value);
				}
			} catch (JSONException e) {
				Ln.e(e, "Could not find property %1$s on Custom properties JSON");
			}
		}
		return propertiesMap;
    }
	
	protected List<String> getTags(JSONArray json) {
		List<String> tags = new ArrayList<String>();
		try {
			for (int i=0; i<json.length(); i++) {
				String tag = json.getString(i);
				tags.add(tag);
			}
		} catch (JSONException e) {
			Ln.e(e, "Error while trying to obtain tags for notification");
		}
		return tags;
	}
	
	public Listener getListener() {
		return listener;
	}

}
