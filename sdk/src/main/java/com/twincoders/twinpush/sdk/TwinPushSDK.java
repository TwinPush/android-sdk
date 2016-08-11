package com.twincoders.twinpush.sdk;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.twincoders.twinpush.sdk.communications.requests.notifications.GetInboxRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationDetailsRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest;
import com.twincoders.twinpush.sdk.entities.LocationPrecision;
import com.twincoders.twinpush.sdk.entities.TwinPushOptions;
import com.twincoders.twinpush.sdk.notifications.PushNotification;

import java.util.List;
import java.util.Map;

public abstract class TwinPushSDK {

    /* Callbacks */
    public interface OnRegistrationListener {
        void onRegistrationError(Exception exception);
        void onRegistrationSuccess(String deviceAlias);
    }

    private static TwinPushSDK sharedInstance = null;
	
	/* Public instance getter */
    /**
     * Obtains a shared instance of the TwinPush SDK for the given context
     */
    public static TwinPushSDK getInstance(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new DefaultTwinPushSDK(context);
        }
        return sharedInstance;
    }
	
	/* Public API Methods */
	
	/* Register methods */

    /**
     * Registers the device on TwinPush service without assigning any alias. If the device already has an alias, it will be kept.
     * This method will register to GCM previously if needed
     */
    public abstract void register();

    /**
     * Registers the device on TwinPush service with the given alias. This method will register to GCM previously if needed
     * @param deviceAlias Alias to assign to device. If alias parameter is null, previously set alias will remain
     */
    public abstract void register(final String deviceAlias);

    /**
     * Registers the device on TwinPush service with the given alias. This method will register to GCM previously if needed
     * @param deviceAlias Alias to assign to device. If alias parameter is null, previously set alias will remain
     * @param listener Listener to be notified of the result of the registration process. Can be null if result is not relevant.
     */
    public abstract void register(final String deviceAlias, final OnRegistrationListener listener);
    
	/* Obtain notifications methods */

    /**
     * Method to obtain notifications sent to current device
     * @param page Page to obtain notifications from. First page is 0
     * @param resultsPerPage Number of results to obtain per page
     * @param listener Listener object to notify result to
     */
    public abstract void getNotifications(int page, int resultsPerPage, GetNotificationsRequest.Listener listener);

    /**
     * Method to obtain notifications sent to current device
     * @param page Page to obtain notifications from. First page is 0
     * @param resultsPerPage Number of results to obtain per page
     * @param tags List of the tags that the notifications must contain to be returned
     * @param noTags List of the tags that the notifications must not contain to be returned
     * @param ignoreNonRichNotifications Defines if the non rich notifications will be ignored (if true) from the search results
     * @param listener Listener object to notify result to
     */
    public abstract void getNotifications(int page, int resultsPerPage, List<String> tags, List<String> noTags, boolean ignoreNonRichNotifications ,GetNotificationsRequest.Listener listener);

    /**
     * Method to obtain notifications sent to current user identified by alias.
     * The request will fail if no alias is associated with the current device.
     * @param page Page to obtain notifications from. First page is 0
     * @param resultsPerPage Number of results to obtain per page
     * @param listener Listener object to notify result to
     */
    public abstract void getUserInbox(int page, int resultsPerPage, GetInboxRequest.Listener listener);

    /**
     * Obtains the details for the selected notification
     * @param notificationId Notification id
     * @param listener Listener object to notify result to
     */
    public abstract void getNotification(String notificationId, GetNotificationDetailsRequest.Listener listener);
    
    /* Properties */
    /**
     * Set the String value to a custom property for the current device
     * @param name Name of the custom property
     * @param value Value to set. If null, previous value will be deleted.
     */
    public abstract void setProperty(String name, String value);

    /**
     * Set the Boolean value to a custom property for the current device
     * @param name Name of the custom property
     * @param value Value to set. If null, previous value will be deleted.
     */
    public abstract void setProperty(String name, Boolean value);

    /**
     * Set the Integer value to a custom property for the current device
     * @param name Name of the custom property
     * @param value Value to set. If null, previous value will be deleted.
     */
    public abstract void setProperty(String name, Integer value);

    /**
     * Set the Float value to a custom property for the current device
     * @param name Name of the custom property
     * @param value Value to set. If null, previous value will be deleted.
     */
    public abstract void setProperty(String name, Float value);

    /**
     * Set the Float value to a custom property for the current device
     * @param name Name of the custom property
     * @param value Value to set. If null, previous value will be deleted.
     */
    public abstract void setProperty(String name, Double value);

    /**
     * Clears the properties registered for the current device
     */
    public abstract void clearProperties();
    
    /* Location */

    /**
     *  Starts monitoring user location and sends to TwinPush significant changes.
     *  Uses the LocationPrecision.MEDIUM by default
     */
    public abstract void startMonitoringLocationChanges();

    /**
     *  Starts monitoring user location and sends to TwinPush significant changes.
     */
    public abstract void startMonitoringLocationChanges(LocationPrecision precision);

    /**
     * Returns true if the application is already monitoring changes on the device location
     */
    public abstract boolean isMonitoringLocationChanges();

    /**
     *  Stops monitoring user location
     */
    public abstract void stopMonitoringLocationChanges();

    /**
     * Updates user current location 
     */
    public abstract void setLocation(double latitude, double longitude);

    /**
     * Updates user current location 
     */
    public abstract void setLocation(Location location);

    /**
     * Check the different location sources from device to obtain user location with medium precision
     */
    public abstract void updateLocation();

    /**
     * Check the different location sources from device to obtain user location with required precision
     */
    public abstract void updateLocation(LocationPrecision precision);
    
    /* Use statistics */

    /**
     * Notifies the start of an activity in order to register user application use
     * @param activity
     */
    public abstract void activityStart(Activity activity);

    /**
     * Notifies the stop of an activity in order to register user application use
     * @param activity
     */
    public abstract void activityStop(Activity activity);

    /**
     * Notifies that the user has opened a received notification
     * @param notification
     */
    public abstract void onNotificationOpen(PushNotification notification);
    
    // API Setup methods

    /**
     * Setup TwinPush SDK with the needed parameters
     * @return true if the setup is OK, false if any of the required parameters is missing
     */
    public abstract boolean setup(TwinPushOptions options);

    /**
     * Obtains previously set resource ID for notifications small icon (shown on notifications or status bar)
     */
    public abstract int getNotificationSmallIcon();

    /**
     * @return Last alias used to register this device
     */
    public abstract String getDeviceAlias();

    /**
     * Returns the device ID assigned by TwinPush API
     * @return Device ID assigned by TwinPush API
     */
    public abstract String getDeviceId();

    /**
     * Returns the GCM Project number (aka Sender ID)
     * @return Project number previously setup
     */
    public abstract String getGcmProjectNumber();

    /**
     * Obtains previously set TwinPush Application API Key
     */
    public abstract String getApiKey();

    /**
     * Obtains previously setup TwinPush App Id
     * @return Application ID
     */
    public abstract String getAppId();

    /**
     * Obtains the last obtained location
     */
    public abstract Location getLastKnownLocation();

    /**
     *  Minimum time required between location updates.
     */
    public abstract long getLocationMinUpdateTime();

    /**
     * Minimum distance before we require a location update.
     */
    public abstract int getLocationMinUpdateDistance();

    // Security

    /**
     * Includes a SSL certificate pinning to check the Public Key
     * @param encodedKey Encoded public key to check
     */
    public abstract void setSSLPublicKeyCheck(String encodedKey);

    /**
     * Obtains the SSL certificate Public Key check previously set
     * @return
     */
    public abstract String getSSLPublicKeyCheck();

    /**
     * Adds a SSL certificate pinning check. It will validate that the given issuer field will have the expected value
     * @param field Issuer field to check (i.e. "CN" for Certificate Name, "O" for Organization)
     * @param expectedValue Value that should match the certificate to be considered valid
     */
    public abstract void addSSLIssuerCheck(String field, String expectedValue);

    /**
     * Adds a SSL certificate pinning check. It will validate that the given subject field will have the expected value
     * @param field Issuer field to check (i.e. "CN" for Certificate Name, "O" for Organization)
     * @param expectedValue Value that should match the certificate to be considered valid
     */
    public abstract void addSSLSubjectCheck(String field, String expectedValue);

    /**
     * Obtains the map of checks for the SSL Certificate Issuer
     * @return
     */
    public abstract Map<String, String> getSSLIssuerChecks();

    /**
     * Obtains the map of checks for the SSL Certificate Subject
     * @return
     */
    public abstract Map<String, String> getSSLSubjectChecks();

    /**
     * Obtains the current subdomain for the TwinPush server. Default subdomain is 'app'
     */
    public abstract String getSubdomain();

    /**
     * Obtains the host for the TwinPush server. Default is "https://{{subdomain}}.twinpush.com"
     */
    public abstract String getServerHost();
}
