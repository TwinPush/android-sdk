package com.yellowpineapple.offers101.communications;

import com.loopj.android.http.AsyncHttpClient;

import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DefaultRequest implements Request {
	
	/* Class settings */
    /** Request name, defines the request type */
    protected String requestName;
    /** Params that will be included in request */
    protected List<RequestParam> requestParams = new ArrayList<RequestParam>();
    /** Reference to class that will execute the request */
    private RequestLauncher requestLauncher;
    /** HTTP Method used to launch the request */
    protected HttpMethod httpMethod = HttpMethod.POST;
    /** Request URL */
    protected String url = "";
    protected String contentType = "";
    protected boolean dummy = false;
    private Map<String, String> headers = new HashMap<String, String>();
    
    private List<OnRequestFinishListener> onRequestFinishListeners = new ArrayList<OnRequestFinishListener>();

    private Boolean canceled = false;

    public void addParam(String key, Object value) {
        if (value != null) {
            requestParams.add(new DefaultRequestParam(key, value));
        }
    }
    
    @Override
    public void addParam(RequestParam param) {
    	requestParams.add(param);
    }

	public String getName() {
		return requestName;
	}

	public List<RequestParam> getParams() {
		return requestParams;
	}

	public void setRequestLauncher(RequestLauncher requestLauncher) {
        this.requestLauncher = requestLauncher;
    }

    public Boolean isCanceled() {
        return canceled;
    }

    @Override
	public void launch() {
    	canceled = false;
        this.requestLauncher.launchRequest(this);
    }

    @Override
    public void cancel() {
        canceled = true;
        this.requestLauncher.cancelRequest(this);
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }
    
    public void onRequestError(Exception exception) {
    	if (!isCanceled()) {
    		getListener().onError(exception);
    		notifyFinishListeners();
    	}
    }

    @Override
    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

	@Override
	public String getURL() {
		return this.url;
	}
	
	@Override
	public String getEncoding() {
		return HTTP.UTF_8;
	}

	@Override
	public String getBodyContent() {
		return "";
	}
	
	@Override
	public String getContentType() {
		return contentType;
	}

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
	public void onSetupClient(AsyncHttpClient client) {
	}

	@Override
	public RequestLauncher getRequestLauncher() {
		return this.requestLauncher;
	}
	
	@Override
	public boolean isDummy() {
		return this.dummy;
	}
	
	@Override
	public boolean isHttpResponseStatusValid(int httpResponseStatusCode) {
		return false;
	}
	
	public void addOnRequestFinishListener(OnRequestFinishListener listener) {
		if (!onRequestFinishListeners.contains(listener)) {
			onRequestFinishListeners.add(listener);
		}
	}
	
	protected void notifyFinishListeners() {
		for (OnRequestFinishListener listener : onRequestFinishListeners) {
			listener.onRequestFinish();
		}
	}
}
