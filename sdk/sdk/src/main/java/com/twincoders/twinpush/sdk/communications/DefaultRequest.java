package com.twincoders.twinpush.sdk.communications;
import com.twincoders.twinpush.sdk.communications.asyhttp.AsyncHttpClient;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.protocol.HTTP;

public abstract class DefaultRequest implements TwinRequest {
	
	/* Class settings */
    /** Request name, defines the request type */
    protected String requestName;
    /** Params that will be included in request */
    protected List<TwinRequestParam> requestParams = new ArrayList<TwinRequestParam>();
    /** Reference to class that will execute the request */
    private TwinRequestLauncher requestLauncher;
    /** HTTP Method used to launch the request */
    protected HttpMethod httpMethod = HttpMethod.POST;
    /** Request URL */
    protected String url = "";
    protected String contentType = "";
    protected boolean dummy = false;
    
    private List<OnRequestFinishListener> onRequestFinishListeners = new ArrayList<TwinRequest.OnRequestFinishListener>(); 
    private String encoding = HTTP.UTF_8;

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

	public String getName() {
		return requestName;
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
	public String getURL() {
		return this.url;
	}
	
	@Override
	public String getEncoding() {
		return this.encoding;
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
	public void onSetupClient(AsyncHttpClient client) {
	}

	@Override
	public TwinRequestLauncher getRequestLauncher() {
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
