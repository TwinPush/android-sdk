package com.twincoders.twinpush.sdk.communications.requests.forms;

import java.util.Map;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public class ReportFormRequest extends TwinPushRequest {
	
	public interface Listener extends ErrorListener {
		void onSuccess();
	}
	
	/* Constants */
	/* Segments */
	private final static String BASE_URL = "https://forms.twinpush.com";
	private final static String REPORT_SEGMENT = "report";
	/* Parameters */
	private final static String REPORTER_TOKEN = "reporter_token";
	private final static String NOTIFICATION_ID = "notification_id";
	private final static String TITLE = "title";
	private final static String MESSAGE = "message";
	private final static String DEVICE_ID = "device_id";
	private final static String ALIAS = "alias";
	private final static String FORM = "form";
	
	/* Properties */
	Listener listener;
	
	public ReportFormRequest(String deviceId, String alias, String appToken, String reporterToken, PushNotification notification, Map<String, Object> form, Listener listener) {
		super(appToken);
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(REPORT_SEGMENT);
		// Parameters
		addParam(REPORTER_TOKEN, reporterToken);
		if (notification != null) {
			addParam(NOTIFICATION_ID, notification.getId());
			addParam(TITLE, notification.getTitle());
			addParam(MESSAGE, notification.getMessage());
		}
		addParam(DEVICE_ID, deviceId);
		addParam(ALIAS, alias);
		addParam(FORM, form);
	}
	
	@Override
	public String getBaseURL() {
		return BASE_URL;
	}
	
	@Override
	public void onResponseProcess(String response) {
		getListener().onSuccess();
	}
	
	public Listener getListener() {
		return listener;
	}

	@Override
	protected void onResponseProcess(JSONObject response) { /* Unused method */ }

	@Override
	protected void onSuccess(JSONObject response) { /* Unused method */ }

}
