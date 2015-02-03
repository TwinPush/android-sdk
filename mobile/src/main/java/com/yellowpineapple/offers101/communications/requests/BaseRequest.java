package com.yellowpineapple.offers101.communications.requests;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.loopj.android.http.AsyncHttpClient;
import com.yellowpineapple.offers101.communications.RESTJSONRequest;
import com.yellowpineapple.offers101.communications.RequestClient;

public abstract class BaseRequest extends RESTJSONRequest {

    RequestClient.Environment environment;

    @Override
	public String getBaseURL() {
		return environment.getUrl();
	}

	@Override
	public void onSetupClient(AsyncHttpClient client) {
		super.onSetupClient(client);
	}

    @Override
	protected void onResponseProcess(JsonElement response) {
        onSuccess(response);
    }

    protected abstract void onSuccess(JsonElement response);

	public void setEnvironment(RequestClient.Environment environment) {
		this.environment = environment;
        this.dummy = environment.isDummy();
	}

    public boolean isHttpResponseStatusValid(int httpResponseStatusCode) {
        return httpResponseStatusCode >= 200 && httpResponseStatusCode < 400;
    }

    protected void addPagination(int page) {
        addParam("page", page);
    }

    protected Gson getParser() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .setDateFormat("yyyy-MM-dd")
                .create();
    }

}