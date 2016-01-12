package com.yellowpineapple.wakup.sdk.communications.requests.search;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yellowpineapple.wakup.sdk.communications.Request;
import com.yellowpineapple.wakup.sdk.communications.requests.BaseRequest;
import com.yellowpineapple.wakup.sdk.models.SearchResult;

import java.lang.reflect.Type;

public class SearchRequest extends BaseRequest {

    /* Segments */
    final static String[] SEGMENTS = new String[] { "search" };

    /* Properties */
    Listener listener;

    public interface Listener extends Request.ErrorListener {
        void onSuccess(SearchResult searchResult);
    }

    public SearchRequest(String query, Listener listener) {
        super();
        this.httpMethod = HttpMethod.GET;
        addSegmentParams(SEGMENTS);
        addParam("q", query);
        this.listener = listener;
    }

    @Override
    protected void onSuccess(JsonElement response) {
        try {
            Type type = new TypeToken<SearchResult>() {}.getType();
            SearchResult searchResult = getParser().fromJson(response, type);
            listener.onSuccess(searchResult);
        } catch (JsonSyntaxException e) {
            getListener().onError(e);
        }
    }

    @Override
    public ErrorListener getListener() {
        return listener;
    }
}
