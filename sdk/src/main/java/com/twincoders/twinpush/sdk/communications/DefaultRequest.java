package com.twincoders.twinpush.sdk.communications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


abstract class DefaultRequest implements TwinRequest {
	
	/* Class settings */
    /** Params that will be included in request */
    private List<TwinRequestParam> requestParams = new ArrayList<TwinRequestParam>();
    /** Reference to class that will execute the request */
    private TwinRequestLauncher requestLauncher;
    /** HTTP Method used to launch the request */
    protected HttpMethod httpMethod = HttpMethod.POST;

    private List<OnRequestFinishListener> onRequestFinishListeners = new ArrayList<>();

    private volatile Boolean canceled = false;
    private boolean finished;
    private Long securityGeneration;

    synchronized void bindSecurityGeneration(long generation) {
        if (securityGeneration == null) securityGeneration = generation;
    }
    synchronized long securityGeneration() { return securityGeneration; }

    public void addParam(String key, Object value) {
        if (value != null) {
        	if (value instanceof List) {
        		List<String> stringList = new ArrayList<>();
        		// Ensure that array contains only strings
        		for (Object item : (List) value) {
        			stringList.add(item.toString());
				}
				requestParams.add(DefaultRequestParam.arrayParam(key, stringList));
			} else {
				requestParams.add(new DefaultRequestParam(key, value));
			}
        }
    }
    
    @Override
    public void addParam(TwinRequestParam param) {
    	requestParams.add(param);
    }

	public List<TwinRequestParam> getParams() {
		return requestParams;
	}

	public void setRequestLauncher(TwinRequestLauncher requestLauncher) {
        this.requestLauncher = requestLauncher;
    }

    public Boolean isCanceled() {
        return canceled;
    }

    @Override
	public void launch() {
    	canceled = false;
        synchronized (this) { finished = false; }
        this.requestLauncher.launchRequest(this);
    }

    @Override
    public void cancel() {
        canceled = true;
        if (this.requestLauncher != null) this.requestLauncher.cancelRequest(this);
        notifyFinishListeners();
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
	public String getEncoding() {
        return "UTF-8";
	}

	@Override
	public String getBodyContent() {
		return "";
	}
	
	@Override
	public Map<String, String> getHeaders() {
		return new HashMap<>();
	}

	@Override
	public TwinRequestLauncher getRequestLauncher() {
		return this.requestLauncher;
	}
	
	@Override
	public boolean isDummy() {
		return false;
	}
	
	@Override
	public boolean isHttpResponseStatusValid(int httpResponseStatusCode) {
		return false;
	}
	
	public synchronized void addOnRequestFinishListener(OnRequestFinishListener listener) {
		if (!onRequestFinishListeners.contains(listener)) {
			onRequestFinishListeners.add(listener);
		}
	}
	
    void notifyFinishListeners() {
        List<OnRequestFinishListener> listeners;
        synchronized (this) {
            if (finished) return;
            finished = true;
            listeners = new ArrayList<>(onRequestFinishListeners);
        }
        for (OnRequestFinishListener listener : listeners) {
            listener.onRequestFinish();
        }
    }
}
