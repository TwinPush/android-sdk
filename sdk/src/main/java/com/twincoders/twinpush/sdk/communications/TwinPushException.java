package com.twincoders.twinpush.sdk.communications;

import androidx.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.twincoders.twinpush.sdk.logging.Ln;

import org.json.JSONObject;

/**
 * Wrapper extension to ease the access to TwinPush API response errors
 */
public class TwinPushException extends Exception {

    private int statusCode = 500;
    private String errorType = null;
    private String errorMessage = null;
    private VolleyError error;

    TwinPushException(VolleyError error) {
        super(error);
        this.error = error;
        NetworkResponse response = error.networkResponse;
        if (response != null) {
            // Try to obtain a more comprehensive error when possible
            try {
                String jsonString =
                        new String(
                                response.data,
                                HttpHeaderParser.parseCharset(response.headers, "UTF-8"));

                JSONObject json = new JSONObject(jsonString);
                JSONObject errorInfo = json.getJSONObject("errors");
                this.errorType = errorInfo.getString("type");
                this.errorMessage = errorInfo.getString("message");
            } catch (Exception ex) {
                Ln.e(ex, "Could not obtain error details from body");
            }
            NetworkResponse networkResponse = error.networkResponse;
            this.statusCode = networkResponse.statusCode;
        }
    }

    /**
     * When available, obtains the response HTTP status code
     * @return HTTP Status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * When available, obtains the error type returned from the TwinPush API service response
     * Possible error types are described in https://developers.twinpush.com/developers/api#common-error-messages
     * @return Error type
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Detailed error description parsed from TwinPush API response
     * @return Error description
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Original volley error obtained from TwinPush API service request
     * @return Volley error
     */
    public VolleyError getError() {
        return error;
    }

    @Nullable
    @Override
    public String getMessage() {
        return errorMessage != null ? errorMessage : super.getMessage();
    }

    @Nullable
    @Override
    public String getLocalizedMessage() {
        return errorMessage != null ? errorMessage : super.getLocalizedMessage();
    }
}
