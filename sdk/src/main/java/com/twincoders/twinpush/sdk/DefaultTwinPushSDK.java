package com.twincoders.twinpush.sdk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.content.ContextCompat;

import com.google.firebase.iid.FirebaseInstanceId;
import com.securepreferences.SecurePreferences;
import com.twincoders.twinpush.sdk.communications.TwinPushRequestFactory;
import com.twincoders.twinpush.sdk.communications.TwinRequest.DefaultListener;
import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetInboxRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetInboxSummaryRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationDetailsRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest.Listener;
import com.twincoders.twinpush.sdk.communications.requests.register.RegisterRequest;
import com.twincoders.twinpush.sdk.entities.InboxNotification;
import com.twincoders.twinpush.sdk.entities.LocationPrecision;
import com.twincoders.twinpush.sdk.entities.PropertyType;
import com.twincoders.twinpush.sdk.entities.RegistrationInfo;
import com.twincoders.twinpush.sdk.entities.TwinPushOptions;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.logging.Strings;
import com.twincoders.twinpush.sdk.notifications.PushNotification;
import com.twincoders.twinpush.sdk.services.LocationChangeReceiver;
import com.twincoders.twinpush.sdk.util.LastLocationFinder;
import com.twincoders.twinpush.sdk.util.StringEncrypter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class DefaultTwinPushSDK extends TwinPushSDK implements LocationListener {

    /* Constants */
    private static final String PREF_FILE_NAME = "TwinPushPrefs";
    private static final String PREF_REGISTRATION_HASH = "REGISTRATION_HASH";
    private static final String PREF_DEVICE_ID = "DEVICE_ID";
    private static final String PREF_DEVICE_UDID = "DEVICE_UDID";
    private static final String PREF_DEVICE_ALIAS = "DEVICE_ALIAS";
    private static final String PREF_TWINPUSH_API_KEY = "TWINPUSH_TOKEN";
    private static final String PREF_TWINPUSH_APP_ID = "TWINPUSH_APP_ID";
    private static final String PREF_TWINPUSH_SUBDOMAIN = "TWINPUSH_SUBDOMAIN";
    private static final String PREF_TWINPUSH_CUSTOM_HOST = "TWINPUSH_CUSTOM_HOST";
    private static final String DEFAULT_SUBDOMAIN = "app";
    private static final String DEFAULT_HOST = "https://%s.twinpush.com";
    // Location constants
    private static final String PREF_LOCATION_LATITUDE = "LOCATION_LATITUDE";
    private static final String PREF_LOCATION_LONGITUDE = "LOCATION_LONGITUDE";
    private static final String PREF_LOCATION_ALTITUDE = "LOCATION_ALTITUDE";
    private static final String PREF_LOCATION_PROVIDER = "LOCATION_PROVIDER";
    private static final String PREF_LOCATION_ACCURACY = "LOCATION_ACCURACY";
    private static final String PREF_LOCATION_TIME = "LOCATION_TIME";
    private static final String PREF_MONITOR_LOCATION_CHANGES = "MONITOR_LOCATION_CHANGES";
    private static final String PREF_LOCATION_MIN_UPDATE_TIME = "LOCATION_MIN_UPDATE_TIME";
    private static final String PREF_LOCATION_MIN_UPDATE_DISTANCE = "LOCATION_MIN_UPDATE_DISTANCE";
    // Security constants
    private static final String PREF_SSL_PUBLIC_KEY = "PREF_SSL_PUBLIC_KEY";
    private static final String PREF_SSL_ISSUER = "PREF_SSL_ISSUER";
    private static final String PREF_SSL_SUBJECT = "PREF_SSL_SUBJECT";

    /* Private properties */
    private Context _context = null;
    private TwinPushRequest registerRequest = null;
    private List<Activity> openedActivities = new ArrayList<>();
    private LastLocationFinder locationFinder = null;

    /* Properties */
    private String deviceAlias = null;
    private String deviceId = null;
    private String apiKey = null;
    private String appId = null;

    /* Private constructor */
    DefaultTwinPushSDK(Context context) {
        _context = context.getApplicationContext();
        locationFinder = new LastLocationFinder(context.getApplicationContext());
        locationFinder.setChangedLocationListener(this);
    }

	/* Public API Methods */

    @Override
    public void register() {
        this.register(null);
    }

    @Override
    public void register(String deviceAlias) {
        this.register(deviceAlias, null);
    }

    @Override
    public void register(final String deviceAlias, final OnRegistrationListener listener) {
        String appId = getAppId();
        String apiKey = getApiKey();
        if (appId != null) {
            if (apiKey != null) {
                // Make sure the device has the proper dependencies.
                // Only register if registration info has changed since last register
                String pushToken = FirebaseInstanceId.getInstance().getToken();
                RegistrationInfo info = RegistrationInfo.fromContext(getContext(), getDeviceUDID(), deviceAlias, pushToken);
                String registrationHash = encrypt(info.toString());

                if (!Strings.equals(registrationHash, getRegistrationHash())) {
                    setRegistrationHash(registrationHash);
                    Ln.d("Registration changed! Launching new registration request");
                    if (registerRequest != null) {
                        registerRequest.cancel();
                    }
                    registerRequest = getRequestFactory().
                            register(info,
                                    new RegisterRequest.Listener() {

                                        @Override
                                        public void onError(Exception exception) {
                                            registerRequest = null;
                                            registerError(exception, listener);
                                        }

                                        @Override
                                        public void onRegistrationSuccess(String deviceId, String deviceAlias) {
                                            Ln.i("Device successfully registered on TwinPush (deviceId:%s, alias:%s)", deviceId, deviceAlias);
                                            registerRequest = null;
                                            setDeviceId(deviceId);
                                            setDeviceAlias(deviceAlias);
                                            if (listener != null) {
                                                listener.onRegistrationSuccess(deviceAlias);
                                            }
                                        }
                                    });
                } else {
                    Ln.d("Registration info did not change since last registration");
                    if (listener != null) {
                        listener.onRegistrationSuccess(deviceAlias);
                    }

                }
            } else {
                registerError(new Exception("Token is not setup in TwinPush SDK"), listener);
            }
        } else {
            registerError(new Exception("Application ID is not setup in TwinPush SDK"), listener);
        }
    }

    private void registerError(Exception e, OnRegistrationListener listener) {
        // Sender ID is not setup
        Ln.e(e);
        if (listener != null) {
            listener.onRegistrationError(e);
        }
    }

    @Override
    public void getNotifications(int page, int resultsPerPage, List<String> tags, List<String> noTags, boolean ignoreNonRichNotifications, Listener listener) {
        getRequestFactory().getNotificationInbox(page, resultsPerPage, tags, noTags, ignoreNonRichNotifications, listener);
    }

    @Override
    public void getNotifications(int page, int resultsPerPage, GetNotificationsRequest.Listener listener) {
        getNotifications(page, resultsPerPage, null, null, true, listener);
    }

    @Override
    public void getUserInbox(int page, int resultsPerPage, GetInboxRequest.Listener listener) {
        getRequestFactory().getUserInbox(page, resultsPerPage, listener);
    }

    @Override
    public void getUserInboxSummary(GetInboxSummaryRequest.Listener listener) {
        getRequestFactory().getUserInboxSummary(listener);
    }

    @Override
    public void getNotification(String notificationId, GetNotificationDetailsRequest.Listener listener) {
        getRequestFactory().getNotification(notificationId, listener);
    }

    @Override
    public void deleteNotification(InboxNotification notification, DefaultListener listener) {
        getRequestFactory().deleteNotification(notification, listener);
    }

    /* Properties */
    public void setProperty(String name, String value) {
        setProperty(name, value, PropertyType.STRING);
    }

    public void setProperty(String name, Boolean value) {
        setProperty(name, value, PropertyType.BOOLEAN);
    }

    public void setProperty(String name, Integer value) {
        setProperty(name, value, PropertyType.INTEGER);
    }

    public void setProperty(String name, Float value) {
        setProperty(name, value, PropertyType.FLOAT);
    }

    public void setProperty(String name, Double value) {
        setProperty(name, value, PropertyType.FLOAT);
    }

    private void setProperty(final String name, final Object value, PropertyType type) {
        if (isDeviceRegistered()) {
            DefaultListener listener = getDefaultListener(String.format("Set property '%s' = '%s'", name, value == null? "null" : value.toString()));
            getRequestFactory().setCustomProperty(name, type, value, listener);
        } else {
            Ln.w("Not launching 'Set custom property': Device unregistered");
        }
    }

    public void clearProperties() {
        if (isDeviceRegistered()) {
            DefaultListener listener = getDefaultListener("Clear properties");
            getRequestFactory().clearCustomProperties(listener);
        } else {
            Ln.w("Not launching 'Clear custom properties': Device unregistered");
        }
    }

	/* Location */

    public void setLocation(double latitude, double longitude) {
        Location location = new Location("USER_ENTRY");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setTime(new Date().getTime());
        setLocation(location);
    }

    public void setLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        setLastKnownLocation(location);
        DefaultListener listener = getDefaultListener("Update location");
        if (isDeviceRegistered()) {
            getRequestFactory().reportStatistics(latitude, longitude, listener);
        } else {
            Ln.w("Not launching 'Location update': Device unregistered");
        }
    }

    public void updateLocation() {
        updateLocation(LocationPrecision.MEDIUM);
    }

    public void updateLocation(LocationPrecision precision) {
        Location location = locationFinder.getLastBestLocation(precision.getMinUpdateDistance(), precision.getMinUpdateTime());
        if (location != null) {
            setLocation(location.getLatitude(), location.getLongitude());
        }
    }

    // Background location

    private PendingIntent getBackgroundLocationIntent() {
        Intent passiveIntent = new Intent(getContext(), LocationChangeReceiver.class);
        return PendingIntent.getBroadcast(getContext(), 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void startMonitoringLocationChanges() {
        startMonitoringLocationChanges(LocationPrecision.MEDIUM);
    }

    public void startMonitoringLocationChanges(LocationPrecision precision) {
        Ln.i("Registering for location updates");
        getSharedPreferences().edit().
                putLong(PREF_LOCATION_MIN_UPDATE_TIME, precision.getMinUpdateTime()).
                putInt(PREF_LOCATION_MIN_UPDATE_DISTANCE, precision.getMinUpdateDistance()).apply();
        setMonitoringLocationChanges(true);
        registerForLocationUpdates();
    }

    public void stopMonitoringLocationChanges() {
        setMonitoringLocationChanges(false);
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(getBackgroundLocationIntent());
        }
    }

    public void registerForLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    getLocationMinUpdateTime(),
                    getLocationMinUpdateDistance(),
                    getBackgroundLocationIntent());
        } else {
            Ln.e("Could not start location updates, required permissions not found");
        }
    }

    private void setMonitoringLocationChanges(boolean monitoring) {
        getSharedPreferences().edit().putBoolean(PREF_MONITOR_LOCATION_CHANGES, monitoring).apply();
    }

    public boolean isMonitoringLocationChanges() {
        return getSharedPreferences().getBoolean(PREF_MONITOR_LOCATION_CHANGES, false);
    }

	/* Use statistics */

    public void activityStart(Activity activity) {
        if (openedActivities.isEmpty()) {
            onApplicationOpen();
        }
        if (!openedActivities.contains(activity)) {
            openedActivities.add(activity);
        }
    }

    public void activityStop(Activity activity) {
        if (openedActivities.contains(activity)) {
            openedActivities.remove(activity);
        }
        if (openedActivities.isEmpty()) {
            onApplicationClose();
        }
    }

    private void onApplicationOpen() {
        if (isDeviceRegistered()) {
            DefaultListener listener = getDefaultListener("On Application Open");
            getRequestFactory().openApp(listener);
        } else {
            Ln.w("Not launching 'On application open event': Device unregistered");
        }
    }

    private void onApplicationClose() {
        if (isDeviceRegistered()) {
            DefaultListener listener = getDefaultListener("On Application Close");
            getRequestFactory().closeApp(listener);
        } else {
            Ln.w("Not launching 'On application close event': Device unregistered");
        }
    }

    @Override
    public void onNotificationOpen(String notificationId) {
        if (notificationId != null) {
            DefaultListener listener = getDefaultListener(String.format("On Notification Open: %s", notificationId));
            getRequestFactory().openNotification(notificationId, listener);
        }
    }

    @Override
    public void onNotificationOpen(PushNotification notification) {
        if (notification != null) {
            onNotificationOpen(notification.getId());
        }
    }

    /* Storage */
    private Map<String, SharedPreferences> sharedPreferencesMap = new TreeMap<>();

    private SharedPreferences getSharedPreferences() {
        return getSharedPreferences(PREF_FILE_NAME);
    }

    private SharedPreferences getSharedPreferences(String preferencesName) {
        SharedPreferences prefs = sharedPreferencesMap.get(preferencesName);
        if (prefs == null) {
            prefs = new SecurePreferences(getContext(), "", String.format(Locale.ENGLISH, "%sSec", preferencesName));
            SharedPreferences oldPrefs = getContext().getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
            // Check if old
            if (!oldPrefs.getAll().isEmpty()) {
                securePreferences(prefs, oldPrefs);
            }
            sharedPreferencesMap.put(preferencesName, prefs);
        }
        return prefs;
    }

    private void securePreferences(SharedPreferences securePreferences, SharedPreferences oldPrefs) {
        // Migrates values stored in old preferences to secured preferences
        SharedPreferences.Editor editor = securePreferences.edit();
        if( !oldPrefs.getAll().isEmpty() )
        {
            // Move everything over.
            for(Map.Entry<String, ?> entry : oldPrefs.getAll().entrySet())
            {
                Object value = entry.getValue();

                if( value instanceof String ) {
                    editor.putString(entry.getKey(), (String) value);
                }
                else if( value instanceof Integer ) {
                    editor.putInt(entry.getKey(), (Integer) value);
                }
                else if( value instanceof Long ) {
                    editor.putLong(entry.getKey(), (Long) value);
                }
                else if( value instanceof Float ) {
                    editor.putFloat(entry.getKey(), (Float) value);
                }
                else if( value instanceof Boolean ) {
                    editor.putBoolean(entry.getKey(), (Boolean) value);
                }
            }

            editor.apply();
            // Clear old prefs.
            oldPrefs.edit().clear().apply();
        }
    }

    /* Getters & Setters */
    public Context getContext() {
        return _context;
    }

    @Override
    public String getDeviceAlias() {
        if (deviceAlias == null) {
            deviceAlias = decrypt(getSharedPreferences().getString(PREF_DEVICE_ALIAS, null));
        }
        return deviceAlias;
    }

    private void setDeviceAlias(String deviceAlias) {
        getSharedPreferences().edit().putString(PREF_DEVICE_ALIAS, encrypt(deviceAlias)).apply();
        this.deviceAlias = deviceAlias;
    }

    @Override
    public String getDeviceId() {
        if (deviceId == null) {
            deviceId = getSharedPreferences().getString(PREF_DEVICE_ID, null);
        }
        return deviceId;
    }

    private void setDeviceId(String deviceId) {
        getSharedPreferences().edit().putString(PREF_DEVICE_ID, deviceId).apply();
        this.deviceId = deviceId;
    }

    @Override
    public String getApiKey() {
        if (apiKey == null) {
            apiKey = getSharedPreferences().getString(PREF_TWINPUSH_API_KEY, null);
        }
        return apiKey;
    }

    private void setApiKey(String apiKey) {
        getSharedPreferences().edit().putString(PREF_TWINPUSH_API_KEY, apiKey).apply();
        this.apiKey = apiKey;
    }

    public String getAppId() {
        if (appId == null) {
            appId = getSharedPreferences().getString(PREF_TWINPUSH_APP_ID, null);
        }
        return appId;
    }

    private void setAppId(String appId) {
        getSharedPreferences().edit().putString(PREF_TWINPUSH_APP_ID, appId).apply();
        this.appId = appId;
    }

    public long getLocationMinUpdateTime() {
        return getSharedPreferences().getLong(PREF_LOCATION_MIN_UPDATE_TIME, 0);
    }

    public int getLocationMinUpdateDistance() {
        return getSharedPreferences().getInt(PREF_LOCATION_MIN_UPDATE_DISTANCE, 0);
    }

    @Override
    public void setDeviceUDID(String deviceUDID) {
        getSharedPreferences().edit().putString(PREF_DEVICE_UDID, deviceUDID).apply();
    }

    @SuppressLint("HardwareIds")
    private String getDeviceUDID() {
        return getSharedPreferences().getString(PREF_DEVICE_UDID, Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID));
    }

    @Override
    public boolean setup(TwinPushOptions options) {
        if (options != null) {
            String appId = options.twinPushAppId;
            String apiKey = options.twinPushApiKey;
            String subdomain = options.subdomain;
            String serverHost = options.serverHost;

            boolean validSetup = Strings.notEmpty(appId) && Strings.notEmpty(apiKey);
            boolean validHost = Strings.notEmpty(subdomain) || Strings.notEmpty(serverHost);

            if (validSetup) {
                if (validHost) {
                    setAppId(options.twinPushAppId);
                    setApiKey(options.twinPushApiKey);
                    if (options.serverHost != null) {
                        setServerHost(options.serverHost);
                    } else {
                        setSubdomain(options.subdomain);
                    }
                    resetSSLChecks();
                    return true;
                } else {
                    Ln.e("TwinPush Setup Error: subdomain or serverHost are required");
                }
            } else {
                Ln.e("TwinPush Setup Error: some of the required fields are missing");
            }
        } else {
            Ln.e("TwinPush Setup Error: options object is null");
        }
        return false;
    }

    private TwinPushRequestFactory getRequestFactory() {
        return TwinPushRequestFactory.getSharedinstance(getContext());
    }

    private DefaultListener getDefaultListener(final String requestName) {
        return new DefaultListener() {

            @Override
            public void onError(Exception exception) {
                Ln.e(exception, String.format("Error while trying to send %s request", requestName));
            }

            @Override
            public void onSuccess() {
                Ln.i("Successfuly sent %s request", requestName);
            }
        };
    }

    private void setLastKnownLocation(Location location) {
        if (location != null) {
            getSharedPreferences().edit().
                    putFloat(PREF_LOCATION_LATITUDE, (float) location.getLatitude()).
                    putFloat(PREF_LOCATION_LONGITUDE, (float) location.getLongitude()).
                    putFloat(PREF_LOCATION_ALTITUDE, (float) location.getAltitude()).
                    putFloat(PREF_LOCATION_ACCURACY, location.getAccuracy()).
                    putLong(PREF_LOCATION_TIME, location.getTime()).
                    putString(PREF_LOCATION_PROVIDER, location.getProvider()).
                    apply();
        }
    }

    @Override
    public Location getLastKnownLocation() {
        Location location = null;
        SharedPreferences pref = getSharedPreferences();
        long time = pref.getLong(PREF_LOCATION_TIME, 0);
        if (time > 0) {
            float latitude = pref.getFloat(PREF_LOCATION_LATITUDE, 0);
            float longitude = pref.getFloat(PREF_LOCATION_LONGITUDE, 0);
            float altitude = pref.getFloat(PREF_LOCATION_ALTITUDE, 0);
            float accuracy = pref.getFloat(PREF_LOCATION_ACCURACY, 0);
            String provider = pref.getString(PREF_LOCATION_PROVIDER, "");
            location = new Location(provider);
            location.setAccuracy(accuracy);
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setAltitude(altitude);
            location.setTime(time);
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        setLocation(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private boolean isDeviceRegistered() {
        return getDeviceId() != null;
    }

	/* Security */

    private void resetSSLChecks() {
        // Reset SSL Checks
        setSSLPublicKeyCheck(null);
        getSharedPreferences(PREF_SSL_ISSUER).edit().clear().apply();
        getSharedPreferences(PREF_SSL_SUBJECT).edit().clear().apply();
    }

    public void setSSLPublicKeyCheck(String encodedKey) {
        getSharedPreferences().edit().putString(PREF_SSL_PUBLIC_KEY, encodedKey).apply();
    }

    public String getSSLPublicKeyCheck() {
        return getSharedPreferences().getString(PREF_SSL_PUBLIC_KEY, null);
    }

    public void addSSLIssuerCheck(String field, String expectedValue) {
        getSharedPreferences(PREF_SSL_ISSUER).edit().putString(field, expectedValue).apply();
    }

    public void addSSLSubjectCheck(String field, String expectedValue) {
        getSharedPreferences(PREF_SSL_SUBJECT).edit().putString(field, expectedValue).apply();
    }

    public Map<String, String> getSSLIssuerChecks() {
        Map<String, String> map = new HashMap<>();
        for( Entry<?, ?> entry : getSharedPreferences(PREF_SSL_ISSUER).getAll().entrySet() ) {
            map.put( entry.getKey().toString(), entry.getValue().toString() );
        }
        return map;
    }

    public Map<String, String> getSSLSubjectChecks() {
        Map<String, String> map = new HashMap<>();
        for( Entry<?, ?> entry : getSharedPreferences(PREF_SSL_SUBJECT).getAll().entrySet() ) {
            map.put( entry.getKey().toString(), entry.getValue().toString() );
        }
        return map;
    }

    private String encrypt(String rawValue) {
        if (rawValue != null) {
            try {
                return new StringEncrypter(getDeviceUDID()).encryptString(rawValue);
            } catch (Exception e) {
                Ln.e(e, "Error trying to encrypt string");
            }
        }
        return null;
    }

    private String decrypt(String encryptedValue) {
        if (encryptedValue != null) {
            try {
                return new StringEncrypter(getDeviceUDID()).decryptString(encryptedValue);
            } catch (Exception e) {
                Ln.e(e, "Error trying to decrypt string");
            }
        }
        return null;
    }

    private void setSubdomain(String subdomain) {
        getSharedPreferences().edit().putString(PREF_TWINPUSH_SUBDOMAIN, subdomain).apply();
    }

    public String getSubdomain() {
        return getSharedPreferences().getString(PREF_TWINPUSH_SUBDOMAIN, DEFAULT_SUBDOMAIN);
    }

    private void setServerHost(String serverHost) {
        getSharedPreferences().edit().putString(PREF_TWINPUSH_CUSTOM_HOST, serverHost).apply();
    }

    public String getServerHost() {
        return getSharedPreferences().getString(PREF_TWINPUSH_CUSTOM_HOST, String.format(DEFAULT_HOST, getSubdomain()));
    }

    private void setRegistrationHash(String registrationHash) {
        getSharedPreferences().edit().putString(PREF_REGISTRATION_HASH, registrationHash).apply();
    }

    private String getRegistrationHash() {
        return getSharedPreferences().getString(PREF_REGISTRATION_HASH, null);
    }



}
