package com.twincoders.twinpush.sdk.forms;

import java.util.Map;

import android.content.Context;

import com.twincoders.twinpush.sdk.notifications.PushNotification;

public abstract class TwinFormsSDK {
	
	/* Callbacks */
	public interface ReportListener {
		void onReportError(Exception exception);
		void onReportSuccess();
	}
	
	private static TwinFormsSDK sharedInstance = null;
	
	/* Public instance getter */
	/** 
	 * Obtains a shared instance of the TwinPush SDK for the given context
	 */
	public static TwinFormsSDK getInstance(Context context) {
		if (sharedInstance == null) {
			sharedInstance = new DefaultTwinFormsSDK(context);
		}
		return sharedInstance;
	}
	
	/* Public API Methods */
	
	/**
	 * Reports the form for the given notification
	 * @param notification Notification to be reported
	 * @param form Map of fields with user responses 
	 */
	public abstract void report(final PushNotification notification, final Map<String, Object> form);
	
	/**
	 * Reports the form for the given notification
	 * @param notification Notification to be reported
	 * @param form Map of fields with user responses 
	 * @param listener Listener class to obtain the request result
	 */
	public abstract void report(final PushNotification notification, final Map<String, Object> form, ReportListener listener);
    
    
	/**
	 * Setup Forms SDK with the needed parameters
	 * @param appToken Application Token obtained from provider
	 * @param reporterToken Reporter Token obtained from provider
	 */
	public abstract void setup(String appToken, String reporterToken);
}
