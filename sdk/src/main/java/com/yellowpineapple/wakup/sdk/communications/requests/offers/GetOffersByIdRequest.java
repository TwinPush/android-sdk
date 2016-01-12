package com.yellowpineapple.wakup.sdk.communications.requests.offers;

import android.location.Location;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yellowpineapple.wakup.sdk.communications.requests.BaseRequest;
import com.yellowpineapple.wakup.sdk.communications.requests.OfferListRequestListener;
import com.yellowpineapple.wakup.sdk.models.Offer;

import java.lang.reflect.Type;
import java.util.List;

public class GetOffersByIdRequest extends BaseRequest {

    /* Constants */
	/* Segments */
    final static String[] SEGMENTS = new String[] { "offers", "get" };

    /* Properties */
    OfferListRequestListener listener;

    public GetOffersByIdRequest(List<String> offerIds, Location location, int page, OfferListRequestListener listener) {
        super();
        this.httpMethod = HttpMethod.GET;
        addSegmentParams(SEGMENTS);
        addPagination(page, RESULTS_PER_PAGE);
        addParam("ids", TextUtils.join(",", offerIds));
        addParam("latitude", location.getLatitude());
        addParam("longitude", location.getLongitude());
        this.listener = listener;
    }

    @Override
    protected void onSuccess(JsonElement response) {
        try {
            Type type = new TypeToken<List<Offer>>() {}.getType();
            List<Offer> offers = getParser().fromJson(response, type);
            listener.onSuccess(offers);
        } catch (JsonSyntaxException e) {
            getListener().onError(e);
        }
    }

    @Override
    public ErrorListener getListener() {
        return listener;
    }
}
