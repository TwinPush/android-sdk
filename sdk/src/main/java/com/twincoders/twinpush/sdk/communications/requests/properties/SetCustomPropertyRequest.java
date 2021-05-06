package com.twincoders.twinpush.sdk.communications.requests.properties;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.entities.PropertyType;

import org.json.JSONObject;

public class SetCustomPropertyRequest extends TwinPushRequest {

	/* Constants */
	/* Segments */
	private final static String ACTION_SEGMENT = "set_custom_property";
	/* Parameters */
	private final static String NAME_KEY = "name";
	private final static String VALUE_TYPE_KEY = "type";
	private final static String VALUE_KEY = "value";

	/* Properties */
	private DefaultListener listener;
	
	public SetCustomPropertyRequest(String applicationId, String deviceId, @NonNull String name,
									@NonNull PropertyType valueType, @Nullable Object value,
									DefaultListener listener) {
		super(applicationId, deviceId);
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(ACTION_SEGMENT);
		// Parameters
		addParam(NAME_KEY, name);
		addParam(VALUE_TYPE_KEY, valueType.getValueType());
		addParam(VALUE_KEY, value);
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		getListener().onSuccess();
	}
	
	public DefaultListener getListener() {
		return listener;
	}

}
