package com.twincoders.twinpush.sdk.communications.requests.register;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.logging.Ln;

public class RegisterRequest extends TwinPushRequest {
	
	public interface Listener extends ErrorListener {
		void onRegistrationSuccess(String deviceId, String deviceAlias);
	}
	
	/* Constants */
	/* Segments */
	private final static String APPLICATIONS_SEGMENT = "apps";
	private final static String DEVICES_SEGMENT = "devices";
	/* Parameters */
	private final static String ALIAS_KEY = "alias_device";
	private final static String UDID_KEY = "udid";
	private final static String REGISTRATION_ID_KEY = "regid";
	/* Response fields */
	private final static String RESPONSE_OBJECTS_KEY = "objects";
	private final static String RESPONSE_DEVICE_ALIAS_KEY = "alias_device";
	private final static String RESPONSE_DEVICE_ID_KEY = "id";
	
	/* Properties */
	Listener listener;
	
	public RegisterRequest(String alias, String registrationId, String applicationId, String deviceUDID, Listener listener) {
		super();
		this.sequential = true;
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(APPLICATIONS_SEGMENT);
		addSegmentParam(applicationId);
		addSegmentParam(DEVICES_SEGMENT);
		// Parameters
		addParam(UDID_KEY, deviceUDID);
		addParam(ALIAS_KEY, alias);
		addParam(REGISTRATION_ID_KEY, registrationId);
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
