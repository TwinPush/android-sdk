package com.twincoders.twinpush.sdk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.provider.Settings.Secure;

import com.google.android.gcm.GCMRegistrar;
import com.twincoders.twinpush.sdk.communications.TwinPushRequestFactory;
import com.twincoders.twinpush.sdk.communications.TwinRequest.DefaultListener;
import com.twincoders.twinpush.sdk.communications.TwinRequest.OnRequestFinishListener;
import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest;
import com.twincoders.twinpush.sdk.communications.requests.notifications.GetNotificationsRequest.Listener;
import com.twincoders.twinpush.sdk.communications.requests.register.RegisterRequest;
import com.twincoders.twinpush.sdk.entities.LocationPrecision;
import com.twincoders.twinpush.sdk.entities.PropertyType;
import com.twincoders.twinpush.sdk.logging.Ln;
import com.twincoders.twinpush.sdk.notifications.PushNotification;
import com.twincoders.twinpush.sdk.notifications.TwinPushIntentService;
import com.twincoders.twinpush.sdk.services.LocationService;
import com.twincoders.twinpush.sdk.util.LastLocationFinder;

public class DefaultTwinPushSDK extends TwinPushSDK implements LocationListener {
	
	/* Constants */
	private static final String PREF_FILE_NAME = "TwinPushPrefs";
	private static final String PREF_NOTIFICATION_SMALL_ICON = "NOTIFICATION_SMALL_ICON";
	private static final String PREF_DEVICE_ID = "DEVICE_ID";
	private static final String PREF_DEVICE_ALIAS = "DEVICE_ALIAS";
	private static final String PREF_GCM_SENDER_ID = "GCM_SENDER_ID";
	private static final String PREF_TWINPUSH_TOKEN = "TWINPUSH_TOKEN";
	private static final String PREF_TWINPUSH_APP_ID = "TWINPUSH_APP_ID";
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

	/* Private properties */
	private Context _context = null;
	private TwinPushRequest registerRequest = null;
	private BroadcastReceiver registrationReceiver;
	private List<TwinPushRequest> pendingRequests = new ArrayList<TwinPushRequest>();
	private boolean stopRequests = false;
	private List<Activity> openedActivities = new ArrayList<Activity>();
	private LastLocationFinder locationFinder = null;
	
	/* Properties */
	private String deviceAlias = null;
	private String deviceId = null;
	private int notificationSmallIcon = 0;
	private String gcmSenderId = null;
	private String token = null;
	private String appId = null;
	
	/* Private constructor */
	protected DefaultTwinPushSDK(Context context) {
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
		unregisterReceiver();
		String gcmSenderId = getGCMSenderId();
		String appId = getAppId();
		String token = getToken();
		if (gcmSenderId != null) {
			if (appId != null) {
				if (token != null) {

					// Make sure the device has the proper dependencies.
					GCMRegistrar.checkDevice(getContext());

					// Make sure the manifest was properly set - comment out this line
					// while developing the app, then uncomment it when it's ready.
					GCMRegistrar.checkManifest(getContext());

					// Get GCM registration id
					final String registrationId = GCMRegistrar.getRegistrationId(getContext());
					boolean isRegistered = registrationId != null && registrationId.length() > 0;

					// Check if regid already presents
					if (!isRegistered) {
						registrationReceiver = new RegisterBroadcastReceiver(deviceAlias, listener);
						getContext().registerReceiver(registrationReceiver, new IntentFilter(TwinPushIntentService.ON_REGISTERED_ACTION));
						// Registration is not present, register now with GCM
						GCMRegistrar.register(getContext(), gcmSenderId);
					} else {
						// GCMRegistrar.unregister(getContext());
						// Device is already registered on GCM
						if (registerRequest != null) {
							registerRequest.cancel();
						}
						registerRequest = getRequestFactory().
								createRegisterRequest(deviceAlias, registrationId, appId, getDeviceUDID(), 
										new RegisterRequest.Listener() {

									@Override
									public void onError(Exception exception) {
										Ln.e(exception, "Error while trying to register with TwinPush");
										if (listener != null) {
											listener.onRegistrationError(exception);
										}
									}

									@Override
									public void onRegistrationSuccess(String deviceId, String deviceAlias) {
										Ln.i("Device successfully registered on TwinPush (deviceId:%s, alias:%s", deviceId, deviceAlias);
										GCMRegistrar.setRegisteredOnServer(getContext(), true);
										setDeviceId(deviceId);
										setDeviceAlias(deviceAlias);
										if (listener != null) {
											listener.onRegistrationSuccess(deviceAlias);
										}
									}
								});
						launchRequest(registerRequest);
					}
				} else {
					// Sender ID is not setup
					String errorMessage = "Token is not setup in TwinPush SDK";
					Ln.e(errorMessage);
					if (listener != null) {
						listener.onRegistrationError(new Exception(errorMessage));
					}
				}
			} else {
				// Sender ID is not setup
				String errorMessage = "Application ID is not setup in TwinPush SDK";
				Ln.e(errorMessage);
				if (listener != null) {
					listener.onRegistrationError(new Exception(errorMessage));
				}
			}
		} else {
			// Sender ID is not setup
			String errorMessage = "GCM Sender ID is not setup in TwinPush SDK";
			Ln.e(errorMessage);
			if (listener != null) {
				listener.onRegistrationError(new Exception(errorMessage));
			}
		}
	}
	
	private void unregisterReceiver() {
		if (registrationReceiver != null) {
    		getContext().unregisterReceiver(registrationReceiver);
    		registrationReceiver = null;
    	}
	}
	
	 /**
     * Receiving register events
     * */
    
    private class RegisterBroadcastReceiver extends BroadcastReceiver {
    	
    	private String deviceAlias;
    	private OnRegistrationListener listener;
    	

		private RegisterBroadcastReceiver(String deviceAlias, OnRegistrationListener listener) {
			super();
			this.deviceAlias = deviceAlias;
			this.listener = listener;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(TwinPushIntentService.ON_REGISTERED_ACTION)) {
        		register(deviceAlias, listener);
        	} else if (intent.getAction().equals(TwinPushIntentService.ON_REGISTER_ERROR_ACTION)) {
        		if (listener != null) {
        			listener.onRegistrationError(new Exception(intent.getStringExtra(TwinPushIntentService.EXTRA_REGISTRATION_ERROR)));
        		}
        	}
		}
    }
    
    @Override
    public void getNotifications(int page, int resultsPerPage, List<String> tags, List<String> noTags, boolean ignoreNonRichNotifications, Listener listener) {
    	TwinPushRequest getNotifRequest = getRequestFactory().
    			createGetNotificationsRequest(page, resultsPerPage, tags, noTags, ignoreNonRichNotifications, listener, getAppId(), getDeviceId());
    	launchRequest(getNotifRequest);
    }
    
    @Override
    public void getNotifications(int page, int resultsPerPage, GetNotificationsRequest.Listener listener) {
    	getNotifications(page, resultsPerPage, null, null, true, listener);
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
    	DefaultListener listener = getDefaultListener(String.format("Set property '%s' = '%s'", name, value == null? "null" : value.toString()));
    	TwinPushRequest request = getRequestFactory().createSetCustomPropertyRequest(name, type, value, listener, getAppId(), getDeviceId());
    	launchRequest(request);
    }
    
    public void clearProperties() {
    	DefaultListener listener = getDefaultListener("Clear properties");
    	TwinPushRequest request = getRequestFactory().createClearCustomPropertiesRequest(listener, getAppId(), getDeviceId());
    	launchRequest(request);
    }
    
    /* Location */
    
    public void startMonitoringLocationChanges() {
    	startMonitoringLocationChanges(LocationPrecision.MEDIUM);
    }
    
    public void startMonitoringLocationChanges(LocationPrecision precision) {
    	getSharedPreferences().edit().
    		putLong(PREF_LOCATION_MIN_UPDATE_TIME, precision.getMinUpdateTime()).
    		putInt(PREF_LOCATION_MIN_UPDATE_DISTANCE, precision.getMinUpdateDistance()).commit();
    	setMonitoringLocationChanges(true);
    	getContext().startService(new Intent(getContext(), LocationService.class));
    }
    
    public void stopMonitoringLocationChanges() {
    	setMonitoringLocationChanges(false);
    	getContext().stopService(new Intent(getContext(), LocationService.class));
    }
    
    private void setMonitoringLocationChanges(boolean monitoring) {
    	getSharedPreferences().edit().putBoolean(PREF_MONITOR_LOCATION_CHANGES, monitoring).commit();
    }
    
    public boolean isMonitoringLocationChanges() {
    	return getSharedPreferences().getBoolean(PREF_MONITOR_LOCATION_CHANGES, false);
    }
    
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
	    	TwinPushRequest request = getRequestFactory().createReportStatisticsRequest(latitude, longitude, listener, getDeviceId());
	    	launchRequest(request);
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
    	DefaultListener listener = getDefaultListener("On Application Open");
    	TwinPushRequest request = getRequestFactory().createOpenAppRequest(listener, getDeviceId());
    	launchRequest(request);
    }
    
    private void onApplicationClose() {
    	DefaultListener listener = getDefaultListener("On Application Close");
    	TwinPushRequest request = getRequestFactory().createCloseAppRequest(listener, getDeviceId());
    	launchRequest(request);
    }
    
    @Override
    public void onNotificationOpen(PushNotification notification) {
    	if (notification != null) {
    		DefaultListener listener = getDefaultListener(String.format("On Notification Open: %s", notification.getId()));
    		TwinPushRequest request = getRequestFactory().createOpenNotificationRequest(notification, listener, getDeviceId());
    		launchRequest(request);
    	}
    }
    
    /* Storage */
	private SharedPreferences getSharedPreferences() {
		SharedPreferences prefs = getContext().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
		return prefs;
	}
	
	/* Getters & Setters */
	public Context getContext() {
		return _context;
	}
	
	@Override
	public int getNotificationSmallIcon() {
		if (notificationSmallIcon == 0) {
			notificationSmallIcon = getSharedPreferences().getInt(PREF_NOTIFICATION_SMALL_ICON, 0);
		}
		return notificationSmallIcon;
	}
	
	@Override
	public void setNotificationSmallIcon(int notificationSmallIcon) {
		getSharedPreferences().edit().putInt(PREF_NOTIFICATION_SMALL_ICON, notificationSmallIcon).commit();
		this.notificationSmallIcon = notificationSmallIcon;
	}
	
	@Override
	public String getDeviceAlias() {
		if (deviceAlias == null) {
			deviceAlias = getSharedPreferences().getString(PREF_DEVICE_ALIAS, null);
		}
		return deviceAlias;
	}
	
	private void setDeviceAlias(String deviceAlias) {
		getSharedPreferences().edit().putString(PREF_DEVICE_ALIAS, deviceAlias).commit();
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
		getSharedPreferences().edit().putString(PREF_DEVICE_ID, deviceId).commit();
		this.deviceId = deviceId;
	}
	
	@Override
	public void setGCMSenderId(String gcmSenderId) {
		getSharedPreferences().edit().putString(PREF_GCM_SENDER_ID, gcmSenderId).commit();
		this.gcmSenderId = gcmSenderId;
	}

	public String getGCMSenderId() {
		if (gcmSenderId == null) {
			gcmSenderId = getSharedPreferences().getString(PREF_GCM_SENDER_ID, null);
		}
		return gcmSenderId;
	}
	
	@Override
	public void setToken(String token) {
		getSharedPreferences().edit().putString(PREF_TWINPUSH_TOKEN, token).commit();
		this.token = token;
	}
	
	@Override
	public String getToken() {
		if (token == null) {
			token = getSharedPreferences().getString(PREF_TWINPUSH_TOKEN, null);
		}
		return token;
	}
	
	@Override
	public void setAppId(String appId) {
		getSharedPreferences().edit().putString(PREF_TWINPUSH_APP_ID, appId).commit();
		this.appId = appId;
	}
	
	public String getAppId() {
		if (appId == null) {
			appId = getSharedPreferences().getString(PREF_TWINPUSH_APP_ID, null);
		}
		return appId;
	}
	
	public long getLocationMinUpdateTime() {
		return getSharedPreferences().getLong(PREF_LOCATION_MIN_UPDATE_TIME, 0);
	}
	
	public int getLocationMinUpdateDistance() {
		return getSharedPreferences().getInt(PREF_LOCATION_MIN_UPDATE_DISTANCE, 0);
	}
	
	protected String getDeviceUDID() {
		String deviceId = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
		return deviceId;
	}
	
	@Override
	public void setup(String twinPushAppId, String twinPushToken, String gcmSenderId) {
		setAppId(twinPushAppId);
		setToken(twinPushToken);
		setGCMSenderId(gcmSenderId);
	}
	
	private TwinPushRequestFactory getRequestFactory() {
		return TwinPushRequestFactory.getSharedinstance(getContext());
	}
	
	private void launchRequest(TwinPushRequest request) {
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
			request.launch();
		}
	}
	
	private void launchNextRequest() {
		if (!pendingRequests.isEmpty()) {
			TwinPushRequest nextRequest = pendingRequests.get(0);
			pendingRequests.remove(nextRequest);
			launchRequest(nextRequest);
		}
	}
	
	DefaultListener getDefaultListener(final String requestName) {
		DefaultListener listener = new DefaultListener() {

			@Override
			public void onError(Exception exception) {
				Ln.e(exception, String.format("Error while trying to send %s request", requestName));
			}

			@Override
			public void onSuccess() {
				Ln.i("Successfuly sent %s request", requestName);
			}
		};
		return listener;
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
				commit();
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
	
	boolean isDeviceRegistered() {
		return getDeviceId() != null;
	}
	
}
