package com.yellowpineapple.wakup.sdk.communications;

import android.content.Context;
import android.os.Handler;

import com.yellowpineapple.wakup.sdk.communications.asyhttp.AsyncHttpClient;
import com.yellowpineapple.wakup.sdk.communications.asyhttp.AsyncHttpResponseHandler;
import com.yellowpineapple.wakup.sdk.communications.asyhttp.PersistentCookieStore;
import com.yellowpineapple.wakup.sdk.utils.Ln;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.client.HttpResponseException;
import cz.msebera.android.httpclient.entity.StringEntity;

public class DefaultRequestLauncher implements RequestLauncher {
	
	Context context;
	
	int timeOutSeconds = 15;

	/** Array of active requests */
	private Map<Request, AsyncHttpClient> activeRequests = new HashMap<Request, AsyncHttpClient>();

	/* PUBLIC API */

    public DefaultRequestLauncher(Context context) {
		this.context = context;
	}

	@Override
	public void setTimeOutSeconds(int timeOutSeconds) {
		this.timeOutSeconds = timeOutSeconds;
	}

	@Override
	public void launchRequest(Request request) {
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
	public void cancelRequest(Request request) {
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
	private void executeRequest(final Request request) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.setTimeout(this.timeOutSeconds * 1000);
		asyncHttpClient.setCookieStore(new PersistentCookieStore(context));
        // Request headers
        for (String header : request.getHeaders().keySet()) {
            asyncHttpClient.addHeader(header, request.getHeaders().get(header));
        }
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
		// Launch request
		if (!request.isDummy()) {
			String requestURL = request.getURL();
			Ln.d("Launching request: %s %s", request.getHttpMethod(), requestURL);
			try {
				switch (request.getHttpMethod()) {
				case GET:
					asyncHttpClient.get(requestURL, responseHandler);
					break;
				case POST: {
					String requestContent = request.getBodyContent();
					Ln.d("INPUT: %s", requestContent);
					StringEntity se = new StringEntity(requestContent, request.getEncoding());
					asyncHttpClient.post(getContext(), requestURL, se, request.getContentType(), responseHandler);
					break;
                }
                case PUT: {
                    String requestContent = request.getBodyContent();
                    Ln.d("INPUT: %s", requestContent);
                    StringEntity se = new StringEntity(requestContent, request.getEncoding());
                    asyncHttpClient.put(getContext(), requestURL, se, request.getContentType(), responseHandler);
                    break;
                }
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

	private void requestEnded(Request request) {
		activeRequests.remove(request);
	}
}
