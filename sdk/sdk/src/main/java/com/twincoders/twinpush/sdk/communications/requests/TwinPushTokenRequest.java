package com.twincoders.twinpush.sdk.communications.requests;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.communications.asyhttp.AsyncHttpClient;

public abstract class TwinPushTokenRequest extends TwinPushRequest {
	
	public TwinPushTokenRequest(String appId) {
		super(appId);
	}

	private static final String TOKEN_KEY = "X-TwinPush-REST-API-Token";
	
	@Override
	public void onSetupClient(AsyncHttpClient client) {
		super.onSetupClient(client);
		client.addHeader(TOKEN_KEY, getToken());
	}
	
	protected String getToken() {
		return TwinPushSDK.getInstance(getRequestLauncher().getContext()).getToken();
	}
}
