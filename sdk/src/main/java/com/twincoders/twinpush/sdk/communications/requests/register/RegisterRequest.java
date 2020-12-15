package com.twincoders.twinpush.sdk.communications.requests.register;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushTokenRequest;
import com.twincoders.twinpush.sdk.entities.RegistrationInfo;
import com.twincoders.twinpush.sdk.logging.Ln;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterRequest extends TwinPushTokenRequest {
	
	public interface Listener extends ErrorListener {
		void onRegistrationSuccess(String deviceId, String deviceAlias);
	}
	
	/* Constants */
	/* Segments */
	private final static String DEVICES_SEGMENT = "devices";
	private final static String REGISTER_SEGMENT = "register";
	/* Parameters */
	private final static String PLATFORM_KEY = "platform";
	private final static String ALIAS_KEY = "alias_device";
	private final static String UDID_KEY = "udid";
	private final static String PUSH_TOKEN_KEY = "push_token";
    private final static String APP_VERSION_KEY = "app_version";
    private final static String SDK_VERSION_KEY = "sdk_version";
    private final static String OS_VERSION_KEY = "os_version";
    private final static String OS_VERSION_CODE_KEY = "os_version_code";
    private final static String MANUFACTURER_KEY = "device_manufacturer";
    private final static String DEVICE_MODEL_KEY = "device_model";
    private final static String DEVICE_CODE_KEY = "device_code";
    private final static String LANGUAGE_KEY = "language";

	/* Response fields */
	private final static String RESPONSE_OBJECTS_KEY = "objects";
	private final static String RESPONSE_DEVICE_ALIAS_KEY = "alias_device";
	private final static String RESPONSE_DEVICE_ID_KEY = "id";
	
	/* Properties */
	Listener listener;
	
	public RegisterRequest(String applicationId, RegistrationInfo registrationInfo, Listener listener) {
		super(applicationId);
		this.sequential = true;
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(DEVICES_SEGMENT);
		addSegmentParam(REGISTER_SEGMENT);
		// Parameters
		addParam(PLATFORM_KEY, registrationInfo.getPlatform().getKey());
		addParam(UDID_KEY, registrationInfo.getUdid());
		addParam(ALIAS_KEY, registrationInfo.getDeviceAlias());
        addParam(PUSH_TOKEN_KEY, registrationInfo.getPushToken());
        addParam(APP_VERSION_KEY, registrationInfo.getAppVersion());
        addParam(SDK_VERSION_KEY, registrationInfo.getSdkVersion());
        addParam(OS_VERSION_KEY, registrationInfo.getOsVersion());
        addParam(OS_VERSION_CODE_KEY, registrationInfo.getOsVersionInt());
        addParam(MANUFACTURER_KEY, registrationInfo.getDeviceManufacturer());
        addParam(DEVICE_MODEL_KEY, registrationInfo.getDeviceModel());
        addParam(DEVICE_CODE_KEY, registrationInfo.getDeviceCode());
        addParam(LANGUAGE_KEY, registrationInfo.getLanguage());
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		String deviceAlias = null;
		String deviceId = null;
		try {
			JSONArray objectArray = response.getJSONArray(RESPONSE_OBJECTS_KEY);
			JSONObject deviceObject = objectArray.getJSONObject(0);
			try {
				if (!deviceObject.isNull(RESPONSE_DEVICE_ALIAS_KEY)) {
					deviceAlias = deviceObject.getString(RESPONSE_DEVICE_ALIAS_KEY);
				}
			} catch (JSONException e) {
				Ln.w(e, "Could not find field %1$s on response", RESPONSE_DEVICE_ALIAS_KEY);
			}
			try {
				deviceId = deviceObject.getString(RESPONSE_DEVICE_ID_KEY);
			} catch (JSONException e) {
				Ln.w(e, "Could not find field %1$s on response", RESPONSE_DEVICE_ID_KEY);
			}
		} catch (JSONException e) {
			Ln.w(e, "Could obtain device object on response");
		}
		
		getListener().onRegistrationSuccess(deviceId, deviceAlias);
	}
	
	public Listener getListener() {
		return listener;
	}

}
