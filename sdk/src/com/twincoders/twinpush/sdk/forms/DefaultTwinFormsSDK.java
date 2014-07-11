package com.twincoders.twinpush.sdk.forms;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.communications.TwinPushRequestFactory;
import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.communications.requests.forms.ReportFormRequest;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

public class DefaultTwinFormsSDK extends TwinFormsSDK {
	
	/* Constants */
	private static final String PREF_FILE_NAME = "TwinFormsPrefs";
	private static final String APP_TOKEN = "APP_TOKEN";
	private static final String REPORTER_TOKEN = "REPORTER_TOKEN";

	/* Private properties */
	private Context _context = null;
	private TwinPushRequest request = null;
	TwinPushSDK twinPush = null;
	
	/* Properties */
	private String appToken = null;
	private String reporterToken = null;
	
	/* Private constructor */
	protected DefaultTwinFormsSDK(Context context) {
		_context = context.getApplicationContext();
		twinPush = TwinPushSDK.getInstance(_context);
	}	
	
	/* Public API Methods */

	@Override
	public void report(PushNotification notification, Map<String, Object> form) {
		report(notification, form, null);
	}
	
	public void report(final PushNotification notification, final Map<String, Object> form, final ReportListener listener) {
		if (request != null) request.cancel();
		request = getRequestFactory().createReportFormRequest(twinPush.getDeviceId(), twinPush.getDeviceAlias(), getAppToken(), getReporterToken(), notification, form, new ReportFormRequest.Listener() {
			
			@Override
			public void onError(Exception exception) {
				if (listener != null) listener.onReportError(exception);
				request = null;
			}
			
			@Override
			public void onSuccess() {
				if (listener != null) listener.onReportSuccess();
				request = null;
			}
		});
		request.launch();
	}

	@Override
	public void setup(String appToken, String reporterToken) {
		setAppToken(appToken);
		setReporterToken(reporterToken);
	}
    
    /* Storage */
	private SharedPreferences getSharedPreferences() {
		return getSharedPreferences(PREF_FILE_NAME);
	}

	private SharedPreferences getSharedPreferences(String preferencesName) {
		SharedPreferences prefs = getContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
		return prefs;
	}
	
	/* Getters & Setters */
	public Context getContext() {
		return _context;
	}
	
	public void setAppToken(String token) {
		getSharedPreferences().edit().putString(APP_TOKEN, token).commit();
		this.appToken = token;
	}
	
	public String getAppToken() {
		if (appToken == null) {
			appToken = getSharedPreferences().getString(APP_TOKEN, null);
		}
		return appToken;
	}
	
	public void setReporterToken(String token) {
		getSharedPreferences().edit().putString(REPORTER_TOKEN, token).commit();
		this.reporterToken = token;
	}
	
	public String getReporterToken() {
		if (reporterToken == null) {
			reporterToken = getSharedPreferences().getString(REPORTER_TOKEN, null);
		}
		return reporterToken;
	}
	
	private TwinPushRequestFactory getRequestFactory() {
		return TwinPushRequestFactory.getSharedinstance(getContext());
	}
}
