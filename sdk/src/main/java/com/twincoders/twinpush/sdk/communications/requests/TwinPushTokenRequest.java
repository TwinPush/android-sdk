package com.twincoders.twinpush.sdk.communications.requests;

import com.twincoders.twinpush.sdk.TwinPushSDK;

import java.util.HashMap;
import java.util.Map;

public abstract class TwinPushTokenRequest extends TwinPushRequest {
	
	public TwinPushTokenRequest(String appId) {
		super(appId);
	}

	private static final String TOKEN_KEY = "X-TwinPush-REST-API-Token";
	
	private String getApiToken() {
		return TwinPushSDK.getInstance(getRequestLauncher().getContext()).getApiKey();
	}

	@Override
	public Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.putAll(super.getHeaders());
		headers.put(TOKEN_KEY, getApiToken());
		return headers;
	}
}
