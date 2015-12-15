package com.yellowpineapple.wakup.communications.requests;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.yellowpineapple.wakup.communications.RESTJSONRequest;
import com.yellowpineapple.wakup.communications.RequestClient;
import com.yellowpineapple.wakup.communications.serializers.CategorySerializer;
import com.yellowpineapple.wakup.models.Category;

public abstract class BaseRequest extends RESTJSONRequest {

    public static int FIRST_PAGE = 0;
    public static int LOCATED_RESULTS_PER_PAGE = 50;
    public static int RESULTS_PER_PAGE = 30;

    RequestClient.Environment environment;

    @Override
	public String getBaseURL() {
		return environment.getUrl();
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

    protected void addPagination(int page, int perPage) {
        addParam("page", page);
        addParam("perPage", perPage);
    }

    protected Gson getParser() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .setDateFormat("yyyy-MM-dd")
                .registerTypeAdapter(Category.class, new CategorySerializer())
                .create();
    }

}