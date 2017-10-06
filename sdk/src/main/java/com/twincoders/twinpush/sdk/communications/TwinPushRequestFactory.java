package com.twincoders.twinpush.sdk.communications;

import android.content.Context;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.communications.TwinRequest.DefaultListener;
import com.twincoders.twinpush.sdk.communications.TwinRequest.OnRequestFinishListener;
import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.communications.requests.forms.ReportFormRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.DeleteInboxNotificationRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetInboxRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetInboxSummaryRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationDetailsRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest.Listener;
import com.twincoders.twinpush.sdk.communications.requests.properties.ClearCustomPropertiesRequest;
import com.twincoders.twinpush.sdk.communications.requests.properties.SetCustomPropertyRequest;
import com.twincoders.twinpush.sdk.communications.requests.register.RegisterRequest;
import com.twincoders.twinpush.sdk.communications.requests.statistics.CloseAppRequest;
import com.twincoders.twinpush.sdk.communications.requests.statistics.OpenAppRequest;
import com.twincoders.twinpush.sdk.communications.requests.statistics.OpenNotificationRequest;
import com.twincoders.twinpush.sdk.communications.requests.statistics.ReportStatisticsRequest;
import com.twincoders.twinpush.sdk.entities.InboxNotification;
import com.twincoders.twinpush.sdk.entities.PropertyType;
import com.twincoders.twinpush.sdk.entities.RegistrationInfo;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TwinPushRequestFactory {

	private static TwinPushRequestFactory sharedInstance = null;
	
	/* Properties */
	TwinRequestLauncher requestLauncher;
	String token = null;
	TwinPushSDK twinpush;
	
	private List<TwinPushRequest> pendingRequests = new ArrayList<TwinPushRequest>();
	private boolean stopRequests = false;
	
	
	private String getDeviceId() {
		return twinpush.getDeviceId();
	}
	
	private String getAppId() {
		return twinpush.getAppId();
	}
	
	public static TwinPushRequestFactory getSharedinstance(Context context) {
		if (sharedInstance == null) {
			sharedInstance = new TwinPushRequestFactory(context);
		}
		return sharedInstance;
	}
	
	private TwinPushRequestFactory(Context context) {
		requestLauncher = new DefaultRequestLauncher(context);
		twinpush = TwinPushSDK.getInstance(context);
	}
	
	/* Register */
	
	public TwinPushRequest register(RegistrationInfo registrationInfo, RegisterRequest.Listener listener) {
        TwinPushRequest request = new RegisterRequest(getAppId(), registrationInfo, listener);
        launch(request);
        return request;
    }
	
	/* Notifications */
	
	public TwinPushRequest getNotificationInbox(int page, int resultsPerPage, List<String> tags, List<String> noTags, boolean ignoreNonRichNotifications, Listener listener) {
		TwinPushRequest request = new GetNotificationsRequest(getAppId(), getDeviceId(), page, resultsPerPage, tags, noTags, ignoreNonRichNotifications, listener);
		launch(request);
		return request;
	}

	public TwinPushRequest getUserInbox(int page, int resultsPerPage, GetInboxRequest.Listener listener) {
		TwinPushRequest request = new GetInboxRequest(getAppId(), getDeviceId(), page, resultsPerPage, listener);
		launch(request);
		return request;
	}

	public TwinPushRequest getUserInboxSummary(GetInboxSummaryRequest.Listener listener) {
		TwinPushRequest request = new GetInboxSummaryRequest(getAppId(), getDeviceId(), listener);
		launch(request);
		return request;
	}
	
	public TwinPushRequest getNotification(String notificationId, GetNotificationDetailsRequest.Listener listener) {
		TwinPushRequest request = new GetNotificationDetailsRequest(getAppId(), getDeviceId(), notificationId, listener);
		launch(request);
		return request;
	}

    public TwinPushRequest deleteNotification(InboxNotification inboxNotification, DefaultListener listener) {
        TwinPushRequest request = new DeleteInboxNotificationRequest(getAppId(), getDeviceId(), inboxNotification.getNotification().getId(), listener);
        launch(request);
        return request;
    }
	
	/* Properties */
	
	public TwinPushRequest setCustomProperty(String name, PropertyType valueType, Object value, DefaultListener listener) {
		TwinPushRequest request = new SetCustomPropertyRequest(getAppId(), getDeviceId(), name, valueType, value, listener);
		launch(request);
		return request;
	}
	
	public TwinPushRequest clearCustomProperties(DefaultListener listener) {
		TwinPushRequest request = new ClearCustomPropertiesRequest(getAppId(), getDeviceId(), listener);
		launch(request);
		return request;
	}
	
	/* Statistics */
	
	public TwinPushRequest openApp(DefaultListener listener) {
		TwinPushRequest request = new OpenAppRequest(getAppId(), getDeviceId(), listener);
		launch(request);
		return request;
	}
	
	public TwinPushRequest closeApp(DefaultListener listener) {
		TwinPushRequest request = new CloseAppRequest(getAppId(), getDeviceId(), listener);
		launch(request);
		return request;
	}
	
	public TwinPushRequest reportStatistics(double latitude, double longitude, DefaultListener listener) {
		TwinPushRequest request = new ReportStatisticsRequest(getAppId(), getDeviceId(), latitude, longitude, listener);
		launch(request);
		return request;
	}
	
	public TwinPushRequest openNotification(String notificationId, DefaultListener listener) {
		TwinPushRequest request = new OpenNotificationRequest(getAppId(), getDeviceId(), notificationId, listener);
		launch(request);
		return request;
	}
	
	/* Forms */
	
	public TwinPushRequest reportForm(String deviceId, String alias, String appToken, String reporterToken, PushNotification notification, Map<String, Object> form, ReportFormRequest.Listener listener) {
		TwinPushRequest request = new ReportFormRequest(deviceId, alias, appToken, reporterToken, notification, form, listener);
		launch(request);
		return request;
	}
	
	/* Launch methods */
	
	private void launch(TwinPushRequest request) {
		if (stopRequests) {
			pendingRequests.add(request);
		} else {
			if (request.isSequential()) {
				stopRequests = true;
				request.addOnRequestFinishListener(new OnRequestFinishListener() {

					@Override
					public void onRequestFinish() {
						stopRequests = false;
						launchNextRequest();
					}
				});
			} else {
				launchNextRequest();
			}
			request.setRequestLauncher(requestLauncher);
			request.launch();
		}
	}
	
	private void launchNextRequest() {
		if (!pendingRequests.isEmpty()) {
			TwinPushRequest nextRequest = pendingRequests.get(0);
			pendingRequests.remove(nextRequest);
			launch(nextRequest);
		}
	}
	
}
