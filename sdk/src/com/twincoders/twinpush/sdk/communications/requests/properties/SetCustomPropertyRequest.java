package com.twincoders.twinpush.sdk.communications.requests.properties;

import org.json.JSONObject;

import com.twincoders.twinpush.sdk.communications.requests.TwinPushRequest;
import com.twincoders.twinpush.sdk.entities.PropertyType;

public class SetCustomPropertyRequest extends TwinPushRequest {

	/* Constants */
	/* Segments */
	private final static String APPLICATIONS_SEGMENT = "apps";
	private final static String DEVICES_SEGMENT = "devices";
	private final static String ACTION_SEGMENT = "set_custom_property";
	/* Parameters */
	private final static String NAME_KEY = "name";
	private final static String VALUE_TYPE_KEY = "type";
	private final static String VALUE_KEY = "value";
	/* Property types */
	private final static String TYPE_STRING = "string";
	private final static String TYPE_BOOLEAN = "boolean";
	private final static String TYPE_INTEGER = "integer";
	private final static String TYPE_FLOAT = "float";
	
	/* Properties */
	DefaultListener listener;
	
	public SetCustomPropertyRequest(String name, PropertyType valueType, Object value, DefaultListener listener, String applicationId, String deviceId) {
		super();
		this.listener = listener;
		this.httpMethod = HttpMethod.POST;
		// Segments
		addSegmentParam(APPLICATIONS_SEGMENT);
		addSegmentParam(applicationId);
		addSegmentParam(DEVICES_SEGMENT);
		addSegmentParam(deviceId);
		addSegmentParam(ACTION_SEGMENT);
		// Parameters
		addParam(NAME_KEY, name);
		addParam(VALUE_TYPE_KEY, getTypeString(valueType));
		addParam(VALUE_KEY, value);
	}
	
	@Override
	protected void onSuccess(JSONObject response) {
		getListener().onSuccess();
	}
	
	public DefaultListener getListener() {
		return listener;
	}
	
	private String getTypeString(PropertyType valueType) {
		String type = null;
		switch(valueType) {
		case STRING:  type = TYPE_STRING;  break;
		case BOOLEAN: type = TYPE_BOOLEAN; break;
		case INTEGER: type = TYPE_INTEGER; break;
		case FLOAT:   type = TYPE_FLOAT;   break;
		}
		return type;
	}

}
