package com.twincoders.twinpush.sdk.communications;

import com.android.volley.Request;

import java.util.List;
import java.util.Map;

public interface TwinRequest {

	/* Inner classes */
    /**
     *  Object that will handle request error
     */
    interface ErrorListener {
        void onError(Exception exception);
    }
    /**
     * Object that will handle request success
     */
    interface DefaultListener extends ErrorListener{
        void onSuccess();
    }
    
    interface OnRequestFinishListener {
    	void onRequestFinish();
    }

    /**
     * Available methods for request sending
     */
    enum HttpMethod {
        GET, POST, DELETE
    }
    
    ErrorListener getListener();

    /**
     * Put a value in request params to be included in request
     * @param key Key to identify parameter
     * @param value Value for parameter
     */
    void addParam(String key, Object value);
    
    /**
     * Put a value in request params to be included in request
     * @param param TwinRequestParam parameter to include
     */
    void addParam(TwinRequestParam param);
    
	/**
	 * List of parameters included to the request, maintaining original order.
	 * @return Request parameters
	 */
	List<TwinRequestParam> getParams();

    /**
     * Map with key-value of custom headers for the request
     * @return Headers map
     */
    Map<String, String> getHeaders();
	
	/**
	 * Sets the request launcher to notify launch or cancel request events
	 */
	void setRequestLauncher(TwinRequestLauncher requestLauncher);
	
	/**
	 * request launcher to notify launch or cancel request events
	 */
	TwinRequestLauncher getRequestLauncher(); 
	
	/**
	 * Obtains the complete target URL. In case of GET requests, parameter will be already included. 
	 * @return Complete target URL
	 */
	String getURL();

    Boolean isCanceled();

    /**
     * Method that enqueue the request execution
     */
    void launch();

    /**
     * Cancels the execution of the request
     */
    void cancel();

    /**
     * HTTP method to execute the request
     * @return HTTP Method
     */
    HttpMethod getHttpMethod();
    
    /**
     * Obtains the encoding used for the request content. Default value is UTF8
     * @return Request encoding charset
     */
    String getEncoding();
    
    /**
     * Method called when the request obtains a response
     */
    void onResponseProcess(String response);
    
    /**
     * Method called when an error occurs while processing the request
     */
    void onRequestError(Exception exception);
    
    /**
     * Content for the body of the request 
     * @return Request body content
     */
    String getBodyContent();
    
    /**
     * Type for the content body
     * @return String defining type for the body content
     */
    String getContentType();

    /**
     * Method that define if the request should be executed or if its execution should be only simulated
     */
    boolean isDummy();
    
    /**
     * Method that defines if a HTTP response status code is valid. If the response is false, the system will return 'Connection error' message.
     * This method will only be called when the status code is not 200, so it is not necessary to handle it. Default response is 'false'.
     * @return true if the code is valid, false otherwise
     */
    boolean isHttpResponseStatusValid(int httpResponseStatusCode);
    
    /**
     * Adds a listener to be notified when the request is finished, not depending of its result
     * @param listener
     */
    void addOnRequestFinishListener(OnRequestFinishListener listener);

    /**
     * Obtains the volley request associated with the TwinRequest
     * @return Volley request
     */
    Request getRequest();
	
}
