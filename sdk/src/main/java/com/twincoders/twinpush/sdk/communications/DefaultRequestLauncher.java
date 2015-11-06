package com.twincoders.twinpush.sdk.communications;

import android.content.Context;
import android.os.Handler;

import com.twincoders.twinpush.sdk.TwinPushSDK;
import com.twincoders.twinpush.sdk.communications.asyhttp.AsyncHttpClient;
import com.twincoders.twinpush.sdk.communications.asyhttp.AsyncHttpResponseHandler;
import com.twincoders.twinpush.sdk.communications.asyhttp.PersistentCookieStore;
import com.twincoders.twinpush.sdk.communications.security.TwinPushSSLSocketFactory;
import com.twincoders.twinpush.sdk.communications.security.TwinPushTrustManager;
import com.twincoders.twinpush.sdk.logging.Ln;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.client.HttpResponseException;
import cz.msebera.android.httpclient.entity.StringEntity;

public class DefaultRequestLauncher implements TwinRequestLauncher {
	
	Context context;
	
	int timeOutSeconds = 60;

	/** Array of active requests */
	private Map<TwinRequest, AsyncHttpClient> activeRequests = new HashMap<TwinRequest, AsyncHttpClient>();

	/* PUBLIC API */
	/* Empty constructor will be used when injected with RoboGuice */
	public DefaultRequestLauncher() {
	}
	
	/* Parameterized constructor will be used when RequestLauncher is not injected */
	public DefaultRequestLauncher(Context context) {
		this.context = context;
	}

	@Override
	public void setTimeOutSeconds(int timeOutSeconds) {
		this.timeOutSeconds = timeOutSeconds;
	}

	@Override
	public void launchRequest(TwinRequest request) {
		Ln.v("Starting request: %s", request.getClass().getName());
		// Check if request is already on queue
		if (!activeRequests.containsKey(request)) {
			// Include request in execution queue
			executeRequest(request);
		} else {
			Ln.w("Request already on queue. Ignoring...");
		}
	}

	@Override
	public void cancelRequest(TwinRequest request) {
		// Cancel request by calling linked Http client method
		if (activeRequests.containsKey(request)) {
			AsyncHttpClient asyncHttpClient = activeRequests.get(request);
			asyncHttpClient.cancelRequests(getContext(), true);
			activeRequests.remove(request);
			Ln.v("Request canceled");
		} else {
			Ln.v("Could not cancel request, not currently active");
		}
	}
	
	@Override
	public Context getContext() {
		return context;
	}

	/* PRIVATE METHODS */
	/** Starts request execution */
	private void executeRequest(final TwinRequest request) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.setTimeout(this.timeOutSeconds * 1000);
		asyncHttpClient.setCookieStore(new PersistentCookieStore(context));
		// Include response handler
		AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable throwable, String response) {
				boolean validResponse = false;
				if (throwable instanceof HttpResponseException) {
					int httpResponseCode = ((HttpResponseException) throwable).getStatusCode();
					validResponse = request.isHttpResponseStatusValid(httpResponseCode);
				}
				if (validResponse) {
					this.onSuccess(response);
				} else {
					Ln.w(throwable, "Request failed. Response: %s", response);
					requestEnded(request);
					request.onRequestError(new Exception(throwable));
				}
			}

			@Override
			public void onSuccess(String response) {
				Ln.v("OUTPUT: %s", response);
				requestEnded(request);
				request.onResponseProcess(response);
			}
		};
		// Give the request a chance to setup the client
		request.onSetupClient(asyncHttpClient);
		
		// Include request in active requests map
		activeRequests.put(request, asyncHttpClient);
		
		// Add certificate pinning check
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			TwinPushTrustManager trustManager = new TwinPushTrustManager();
			TwinPushSDK twinPush = TwinPushSDK.getInstance(context);
			trustManager.setPublicKey(twinPush.getSSLPublicKeyCheck());
			trustManager.setIssuerChecks(twinPush.getSSLIssuerChecks());
			trustManager.setSubjectChecks(twinPush.getSSLSubjectChecks());
			TwinPushSSLSocketFactory sf = new TwinPushSSLSocketFactory(trustStore, trustManager);
			sf.setHostnameVerifier(TwinPushSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			asyncHttpClient.setSSLSocketFactory(sf);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		// Launch request
		if (!request.isDummy()) {
			String requestURL = request.getURL();
			Ln.d("Launching request: %s", requestURL);
			try {
				switch (request.getHttpMethod()) {
				case GET:
					asyncHttpClient.get(requestURL, responseHandler);
					break;
				case POST:
					String requestContent = request.getBodyContent();
					Ln.d("INPUT: %s", requestContent);
					StringEntity se = new StringEntity(requestContent, request.getEncoding());
					asyncHttpClient.post(getContext(), requestURL, se, request.getContentType(), responseHandler);
					break;
				case DELETE:
					asyncHttpClient.delete(getContext(), requestURL, responseHandler);
					break;
				}
			} catch (Exception ex) {
				Ln.w(ex, "Request failed");
				requestEnded(request);
				request.onRequestError(ex);
			}
		} else {
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					if (!request.isCanceled()) {
						requestEnded(request);
						request.onResponseProcess("");
					}
				}
			}, 1000);
		}
	}

	private void requestEnded(TwinRequest request) {
		activeRequests.remove(request);
	}
}
