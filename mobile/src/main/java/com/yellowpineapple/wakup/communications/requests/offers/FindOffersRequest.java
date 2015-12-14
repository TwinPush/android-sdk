package com.yellowpineapple.wakup.communications.requests.offers;

import android.location.Location;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yellowpineapple.wakup.communications.requests.BaseRequest;
import com.yellowpineapple.wakup.communications.requests.OfferListRequestListener;
import com.yellowpineapple.wakup.models.Offer;

import java.lang.reflect.Type;
import java.util.List;

public class FindOffersRequest extends BaseRequest {

    /* Constants */
	/* Segments */
    final static String[] SEGMENTS = new String[] { "offers", "find" };

    /* Properties */
    OfferListRequestListener listener;

    public FindOffersRequest(Location location, Double radiusInKm, OfferListRequestListener listener) {
        this(location, false, FIRST_PAGE, LOCATED_RESULTS_PER_PAGE, radiusInKm, listener);
    }

    public FindOffersRequest(Location location, int page, OfferListRequestListener listener) {
        this(location, true, page, RESULTS_PER_PAGE, null, listener);
    }

    public FindOffersRequest(Location location, boolean includeOnline, int page, int perPage, Double radiusInKm, OfferListRequestListener listener) {
        super();
        this.httpMethod = HttpMethod.GET;
        addSegmentParams(SEGMENTS);
        addPagination(page, perPage);
        addParam("includeOnline", includeOnline);
        addParam("latitude", location.getLatitude());
        addParam("longitude", location.getLongitude());
        if (radiusInKm != null) addParam("radiusInKm", radiusInKm);
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
