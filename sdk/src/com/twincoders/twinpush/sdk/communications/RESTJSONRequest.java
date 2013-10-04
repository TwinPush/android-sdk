package com.twincoders.twinpush.sdk.communications;

import org.json.JSONException;
import org.json.JSONObject;

import com.twincoders.twinpush.sdk.logging.Ln;

public abstract class RESTJSONRequest extends RESTRequest {
	
	final static String CONTENT_TYPE_VALUE = "application/json";
	
	@Override
	public String getContentType() {
		return CONTENT_TYPE_VALUE;
	}

	@Override
	public void onResponseProcess(String response) {
		if (!isCanceled()) {
			try {
				JSONObject object = new JSONObject();
				if (response != null && response.trim().length() > 0) {
					object = new JSONObject(response);
				}
				onResponseProcess(object);
			} catch (JSONException e) {
				Ln.e(e);
				onRequestError(e);
			}
			notifyFinishListeners();
		}
	}
	
	protected abstract void onResponseProcess(JSONObject response);

}
