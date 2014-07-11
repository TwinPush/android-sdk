package com.twincoders.twinpush.sdk.communications;

import java.util.List;
import java.util.Map;

import android.content.Context;

import com.twincoders.twinpush.sdk.communications.TwinRequest.DefaultListener;
import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.communications.requests.forms.ReportFormRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest.Listener;
import com.twincoders.twinpush.sdk.communications.requests.properties.ClearCustomPropertiesRequest;
import com.twincoders.twinpush.sdk.communications.requests.properties.SetCustomPropertyRequest;
import com.twincoders.twinpush.sdk.communications.requests.register.RegisterRequest;
import com.twincoders.twinpush.sdk.communications.requests.statistics.CloseAppRequest;
import com.twincoders.twinpush.sdk.communications.requests.statistics.OpenAppRequest;
import com.twincoders.twinpush.sdk.communications.requests.statistics.OpenNotificationRequest;
import com.twincoders.twinpush.sdk.communications.requests.statistics.ReportStatisticsRequest;
import com.twincoders.twinpush.sdk.entities.PropertyType;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public class TwinPushRequestFactory {

	private static TwinPushRequestFactory sharedInstance = null;
	
	/* Properties */
	TwinRequestLauncher requestLauncher;
	String token = null;
	
	public static TwinPushRequestFactory getSharedinstance(Context context) {
		if (sharedInstance == null) {
			sharedInstance = new TwinPushRequestFactory(context);
		}
		return sharedInstance;
	}
	
	private TwinPushRequestFactory(Context context) {
		requestLauncher = new DefaultRequestLauncher(context);
	}
	
	/* Register */
	
	public TwinPushRequest createRegisterRequest(String alias, String registrationId, String applicationId, String deviceUDID, RegisterRequest.Listener listener) {
        TwinPushRequest request = new RegisterRequest(alias, registrationId, applicationId, deviceUDID, listener);
        request.setRequestLauncher(requestLauncher);
        return request;
    }
	
	/* Notifications */
	
	public TwinPushRequest createGetNotificationsRequest(int page, int resultsPerPage, List<String> tags, List<String> noTags, boolean ignoreNonRichNotifications, Listener listener, String applicationId, String deviceId) {
		TwinPushRequest request = new GetNotificationsRequest(page, resultsPerPage, tags, noTags, ignoreNonRichNotifications, listener, applicationId, deviceId);
		request.setRequestLauncher(requestLauncher);
		return request;
	}
	
	/* Properties */
	
	public TwinPushRequest createSetCustomPropertyRequest(String name, PropertyType valueType, Object value, DefaultListener listener, String applicationId, String deviceId) {
		TwinPushRequest request = new SetCustomPropertyRequest(name, valueType, value, listener, applicationId, deviceId);
		request.setRequestLauncher(requestLauncher);
		return request;
	}
	
	public TwinPushRequest createClearCustomPropertiesRequest(DefaultListener listener, String applicationId, String deviceId) {
		TwinPushRequest request = new ClearCustomPropertiesRequest(listener, applicationId, deviceId);
		request.setRequestLauncher(requestLauncher);
		return request;
	}
	
	/* Statistics */
	
	public TwinPushRequest createOpenAppRequest(DefaultListener listener, String deviceId) {
		TwinPushRequest request = new OpenAppRequest(listener, deviceId);
		request.setRequestLauncher(requestLauncher);
		return request;
	}
	
	public TwinPushRequest createCloseAppRequest(DefaultListener listener, String deviceId) {
		TwinPushRequest request = new CloseAppRequest(listener, deviceId);
		request.setRequestLauncher(requestLauncher);
		return request;
	}
	
	public TwinPushRequest createReportStatisticsRequest(double latitude, double longitude, DefaultListener listener, String deviceId) {
		TwinPushRequest request = new ReportStatisticsRequest(latitude, longitude, listener, deviceId);
		request.setRequestLauncher(requestLauncher);
		return request;
	}
	
	public TwinPushRequest createOpenNotificationRequest(PushNotification notification, DefaultListener listener, String deviceId) {
		TwinPushRequest request = new OpenNotificationRequest(notification, listener, deviceId);
		request.setRequestLauncher(requestLauncher);
		return request;
	}
	
	/* Forms */
	
	public TwinPushRequest createReportFormRequest(String deviceId, String alias, String appToken, String reporterToken, PushNotification notification, Map<String, Object> form, ReportFormRequest.Listener listener) {
		TwinPushRequest request = new ReportFormRequest(deviceId, alias, appToken, reporterToken, notification, form, listener);
		request.setRequestLauncher(requestLauncher);
		return request;
	}
	
}
