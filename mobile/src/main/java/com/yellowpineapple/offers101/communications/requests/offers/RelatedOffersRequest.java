package com.yellowpineapple.offers101.communications.requests.offers;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yellowpineapple.offers101.communications.requests.BaseRequest;
import com.yellowpineapple.offers101.communications.requests.OfferListRequestListener;
import com.yellowpineapple.offers101.models.Offer;

import java.lang.reflect.Type;
import java.util.List;

public class RelatedOffersRequest extends BaseRequest {

    /* Constants */
	/* Segments */
    final static String[] SEGMENTS = new String[] { "offers", "related" };

    /* Properties */
    OfferListRequestListener listener;

    public RelatedOffersRequest(Offer offer, int page, int perPage, OfferListRequestListener listener) {
        super();
        this.httpMethod = HttpMethod.GET;
        addSegmentParams(SEGMENTS);
        addPagination(page, perPage);
        if (offer.getStore() != null) {
            addParam("storeId", offer.getStore().getId());
        }
        addParam("offerId", offer.getId());
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
