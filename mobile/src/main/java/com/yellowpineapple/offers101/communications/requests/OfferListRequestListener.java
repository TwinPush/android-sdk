package com.yellowpineapple.offers101.communications.requests;

import com.yellowpineapple.offers101.communications.Request;
import com.yellowpineapple.offers101.models.Offer;

import java.util.List;

public interface OfferListRequestListener extends Request.ErrorListener {
    void onSuccess(List<Offer> offers);
}
