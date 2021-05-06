package com.twincoders.twinpush.sdk.communications;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.twincoders.twinpush.sdk.logging.Ln;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public abstract class RESTJSONRequest extends RESTRequest {
	
	private final static String CONTENT_TYPE_VALUE = "application/json";
    private final static String EMPTY_RESPONSE = "{}";
	
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

    @Override
	public Request getRequest() {
		JSONObject js = null;
        Ln.i("Launching request: %s", getURL());
        if (getHttpMethod() == HttpMethod.POST) {
            js = serializeBodyParams(getParams());
            Ln.i("INPUT: %s", js.toString());
        }
		return new JsonObjectRequest(getRequestMethod(), getURL(),
				js,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
                        Ln.i("OUTPUT: %s", response.length() > 0 ? response.toString() : "OK");
                        onResponseProcess(response);
                        notifyFinishListeners();
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
                        Ln.i(error, "ERROR %s", error.getMessage());
						onRequestError(new TwinPushException(error));
                        notifyFinishListeners();
					}
				}) {
            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                // Allow empty response as valid
                try {
                    if (response.data.length == 0) {
                        byte[] responseData = EMPTY_RESPONSE.getBytes("UTF-8");
                        response = new NetworkResponse(response.statusCode, responseData,
                                response.notModified, response.networkTimeMs,
                                response.allHeaders);
                    }
                } catch (UnsupportedEncodingException e) {
                    Ln.e(e, "Error returning empty result");
                }
                return super.parseNetworkResponse(response);
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return RESTJSONRequest.this.getHeaders();
            }
        };
	}

	private int getRequestMethod() {
		switch (getHttpMethod()) {
			case GET:
				return Request.Method.GET;
			case DELETE:
				return Request.Method.DELETE;
			case POST:
				return Request.Method.POST;
			default:
				return Request.Method.GET;
		}
	}

}
