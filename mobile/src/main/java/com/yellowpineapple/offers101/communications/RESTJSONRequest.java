package com.yellowpineapple.offers101.communications;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.yellowpineapple.offers101.utils.Ln;

public abstract class RESTJSONRequest extends RESTRequest {
	
	final static String CONTENT_TYPE_VALUE = "application/json";

	@Override
	public String getContentType() {
		return CONTENT_TYPE_VALUE;
	}

	@Override
	public void onResponseProcess(String response) {
		if (!isCanceled()) {
            Ln.d("OUTPUT: %s", response);
			try {
                JsonElement jsonElement = JsonNull.INSTANCE;
				if (response != null && response.trim().length() > 0) {
                    JsonParser parser = new JsonParser();
                    jsonElement = parser.parse(response);
				}
				onResponseProcess(jsonElement);
			} catch (JsonSyntaxException e) {
				Ln.e(e);
				onRequestError(e);
			}
			notifyFinishListeners();
		}
	}
	
	protected abstract void onResponseProcess(JsonElement response);

}
