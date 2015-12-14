package com.yellowpineapple.wakup.communications.requests.offers;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yellowpineapple.wakup.communications.requests.BaseRequest;
import com.yellowpineapple.wakup.communications.requests.OfferListRequestListener;
import com.yellowpineapple.wakup.models.Company;
import com.yellowpineapple.wakup.models.Offer;
import com.yellowpineapple.wakup.models.Store;

import java.lang.reflect.Type;
import java.util.List;

public class CompanyOffersRequest extends BaseRequest {

    /* Constants */
	/* Segments */
    final static String[] SEGMENTS = new String[] { "offers", "company" };

    /* Properties */
    OfferListRequestListener listener;

    Company company;
    Store store;

    public CompanyOffersRequest(Company company, Store store, int page, int perPage, OfferListRequestListener listener) {
        super();
        this.httpMethod = HttpMethod.GET;
        this.company = company;
        this.store = store;
        addSegmentParams(SEGMENTS);
        addPagination(page, perPage);
        addParam("companyId", company.getId());
        if (store != null) addParam("storeId", store.getId());
        this.listener = listener;
    }

    @Override
    protected void onSuccess(JsonElement response) {
        try {
            Type type = new TypeToken<List<Offer>>() {}.getType();
            List<Offer> offers = getParser().fromJson(response, type);
            // Assign Company and Store to obtained offers
            for (Offer offer : offers) {
                offer.setCompany(company);
                offer.setStore(store);
            }
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
