package com.twincoders.twinpush.sdk.communications.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.communications.RESTJSONRequest;
import com.twincoders.twinpush.sdk.communications.asyhttp.AsyncHttpClient;

public abstract class TwinPushRequest extends RESTJSONRequest {
	
	/* Constants */
	// TODO Point URL to production
	private static final String TWINPUSH_URL = "http://beta.twinpush.com/api/v2";
	private static final String TOKEN_KEY = "X-TwinPush-REST-API-Token";
	/* Status code */
	private static final int STATUS_CODE_INVALID_ELEMENT = 422;
	private static final int STATUS_CODE_FORBIDDEN = 403;
	private static final int STATUS_CODE_EMPTY = 404;
	
	/* Result */
	private static final String ERRORS_KEY = "errors";
	private static final String ERROR_MESSAGE_KEY = "message";
	
	/* Determines if other requests should wait until this is finished */
	protected boolean sequential = false;
	
	@Override
	public String getBaseURL () {
		return TWINPUSH_URL;
	}
	
	@Override
	public void onSetupClient(AsyncHttpClient client) {
		super.onSetupClient(client);
		client.addHeader(TOKEN_KEY, getToken());
	}
	
	protected String getToken() {
		return TwinPushSDK.getInstance(getRequestLauncher().getContext()).getToken();
	}
	
	@Override
	public boolean isHttpResponseStatusValid(int httpResponseStatusCode) {
		return httpResponseStatusCode == STATUS_CODE_INVALID_ELEMENT || httpResponseStatusCode == STATUS_CODE_FORBIDDEN || httpResponseStatusCode == STATUS_CODE_EMPTY;
	}

	@Override
	protected void onResponseProcess(JSONObject response) {
		try {
			if (response.isNull(ERRORS_KEY)) {
				// Handle success response
				onSuccess(response);
			} else {
				JSONObject errorsJson = response.getJSONObject(ERRORS_KEY);
				String errorMessage = null;
				Object errorMessageObj = errorsJson.get(ERROR_MESSAGE_KEY);
				if (errorMessageObj instanceof String) {
					errorMessage = (String) errorMessageObj;
				} else {
					JSONArray errorsMessages = errorsJson.getJSONArray(ERROR_MESSAGE_KEY);
					StringBuilder builder = new StringBuilder();
					for (int i=0; i<errorsMessages.length(); i++) {
						if (i > 0) {
							builder.append("\n");
						}
						builder.append(errorsMessages.getString(i));
					}
					errorMessage = builder.toString();
				}
				getListener().onError(new Exception(errorMessage));
			}
		} catch (JSONException e) {
			getListener().onError(e);
		}
	}
	
	public boolean isSequential() {
		return sequential;
	}
	
	protected abstract void onSuccess(JSONObject response);

}
