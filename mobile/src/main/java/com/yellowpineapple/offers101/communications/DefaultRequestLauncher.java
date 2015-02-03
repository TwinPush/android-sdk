package com.yellowpineapple.offers101.communications;

import android.content.Context;
import android.os.Handler;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.yellowpineapple.offers101.utils.Ln;

import org.apache.http.Header;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                boolean validResponse = false;
                if (error instanceof HttpResponseException) {
                    int httpResponseCode = ((HttpResponseException) error).getStatusCode();
                    validResponse = request.isHttpResponseStatusValid(httpResponseCode);
                }
                if (validResponse) {
                    this.onSuccess(statusCode, headers, responseBody);
                } else {
                    Ln.w(error, "Request failed. Response: %s", error);
                    requestEnded(request);
                    request.onRequestError(new Exception(error));
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String response = responseBody == null ? null : new String(responseBody, getCharset());
                    Ln.v("OUTPUT: %s", response);
                    requestEnded(request);
                    request.onResponseProcess(response);
                } catch (UnsupportedEncodingException e) {
                    Ln.e(e);
                    request.onRequestError(e);
                }
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
