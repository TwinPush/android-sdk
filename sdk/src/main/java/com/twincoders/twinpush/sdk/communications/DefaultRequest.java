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

    private Boolean canceled = false;

    public void addParam(String key, Object value) {
        if (value != null) {
            requestParams.add(new DefaultRequestParam(key, value));
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
        this.requestLauncher.launchRequest(this);
    }

    @Override
    public void cancel() {
        canceled = true;
        if (this.requestLauncher != null) this.requestLauncher.cancelRequest(this);
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
	
	public void addOnRequestFinishListener(OnRequestFinishListener listener) {
		if (!onRequestFinishListeners.contains(listener)) {
			onRequestFinishListeners.add(listener);
		}
	}
	
	void notifyFinishListeners() {
		for (OnRequestFinishListener listener : onRequestFinishListeners) {
			listener.onRequestFinish();
		}
	}
}
