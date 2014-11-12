package com.twincoders.twinpush.sdk.communications.requests;

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

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.communications.RESTJSONRequest;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public abstract class TwinPushRequest extends RESTJSONRequest {
	
	/* Constants */
	private static final String TWINPUSH_URL = "%s/api/v2";
	private final static String APPLICATIONS_SEGMENT = "apps";
	private final static String DEVICES_SEGMENT = "devices";
	private final static String NOTIFICATIONS_SEGMENT = "notifications";
	/* Status code */
	private static final int STATUS_CODE_INVALID_ELEMENT = 422;
	private static final int STATUS_CODE_FORBIDDEN = 403;
	private static final int STATUS_CODE_EMPTY = 404;
	
	/* Result */
	private static final String ERRORS_KEY = "errors";
	private static final String ERROR_MESSAGE_KEY = "message";
	
	/* Determines if other requests should wait until this is finished */
	protected boolean sequential = false;
	
	public TwinPushRequest(String appId) {
		this(appId, null, null);
	}
	public TwinPushRequest(String appId, String deviceId) {
		this(appId, deviceId, null);
	}
	public TwinPushRequest(String appId, String deviceId, String notificationId) {
		super();
		if (appId != null) { 
			addSegmentParam(APPLICATIONS_SEGMENT);
			addSegmentParam(appId);
		}
		if (deviceId != null) {
			addSegmentParam(DEVICES_SEGMENT);
			addSegmentParam(deviceId);
		}
		if (notificationId != null) {
			addSegmentParam(NOTIFICATIONS_SEGMENT);
			addSegmentParam(notificationId);
		}
	}
	
	@Override
	public String getBaseURL () {
		String host = TwinPushSDK.getInstance(getRequestLauncher().getContext()).getServerHost();
		return String.format(TWINPUSH_URL, host);
	}
	
	@Override
	public boolean isHttpResponseStatusValid(int httpResponseStatusCode) {
		return httpResponseStatusCode == STATUS_CODE_INVALID_ELEMENT || httpResponseStatusCode == STATUS_CODE_FORBIDDEN || httpResponseStatusCode == STATUS_CODE_EMPTY;
	}

	@Override
	protected void onResponseProcess(JSONObject response) {
		try {
			if (response.isNull(ERRORS_KEY)) {
				// Handle success response
				onSuccess(response);
			} else {
				JSONObject errorsJson = response.getJSONObject(ERRORS_KEY);
				String errorMessage = null;
				Object errorMessageObj = errorsJson.get(ERROR_MESSAGE_KEY);
				if (errorMessageObj instanceof String) {
					errorMessage = (String) errorMessageObj;
				} else {
					JSONArray errorsMessages = errorsJson.getJSONArray(ERROR_MESSAGE_KEY);
					StringBuilder builder = new StringBuilder();
					for (int i=0; i<errorsMessages.length(); i++) {
						if (i > 0) {
							builder.append("\n");
						}
						builder.append(errorsMessages.getString(i));
					}
					errorMessage = builder.toString();
				}
				getListener().onError(new Exception(errorMessage));
			}
		} catch (JSONException e) {
			getListener().onError(e);
		}
	}
	
	protected String getNullableString(JSONObject json, String name) throws JSONException {
		return json.has(name) && !json.isNull(name) ? json.getString(name) : null;
	}
	
	public boolean isSequential() {
		return sequential;
	}
	
	protected abstract void onSuccess(JSONObject response);
	
	// Parse methods

	private static class NotificationFields {
		final static String ID_KEY = "id";
		final static String TITLE_KEY = "title";
		final static String MESSAGE_KEY = "alert";
		final static String TAGS_KEY = "tags";
		final static String SOUND_KEY = "sound";
		final static String RICH_URL_KEY = "tp_rich_url";
		final static String CUSTOM_PROPERTIES_KEY = "custom_properties";
		final static String DATE = "last_sent_at";
		final static String DATE_ALT = "send_since";
		final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss 'UTC'";
	}
	
	protected PushNotification parseNotification(JSONObject json) throws JSONException {
		PushNotification n = new PushNotification();
		n.setId(getNullableString(json, NotificationFields.ID_KEY));
		n.setTitle(getNullableString(json, NotificationFields.TITLE_KEY));
		n.setMessage(getNullableString(json, NotificationFields.MESSAGE_KEY));
		n.setSound(getNullableString(json, NotificationFields.SOUND_KEY));
		n.setRichURL(getNullableString(json, NotificationFields.RICH_URL_KEY));
		n.setCustomProperties(getCustomPropertiesMap(json.getJSONObject(NotificationFields.CUSTOM_PROPERTIES_KEY)));
		if (!json.isNull(NotificationFields.TAGS_KEY)) {
			n.setTags(getTags(json.getJSONArray(NotificationFields.TAGS_KEY)));
		}
		// Parse date
		SimpleDateFormat dateFormat = new SimpleDateFormat(NotificationFields.DATE_FORMAT, Locale.UK);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date;
		try {
			String dateString = getNullableString(json, json.has(NotificationFields.DATE) ? NotificationFields.DATE : NotificationFields.DATE_ALT);
			if (dateString != null) {
				date = dateFormat.parse(dateString);
			} else {
				date = new Date();
			}
		} catch (ParseException e) {
			Ln.e(e, "Error while trying to parse notification date");
			date = new Date();
			Ln.i("Current format: %s", dateFormat.format(date));
		}
		n.setDate(date);
		return n;
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

}
