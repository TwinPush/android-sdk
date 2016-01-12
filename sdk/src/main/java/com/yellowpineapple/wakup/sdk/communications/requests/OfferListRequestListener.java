package com.yellowpineapple.wakup.sdk.communications.requests;

import com.yellowpineapple.wakup.sdk.communications.Request;
import com.yellowpineapple.wakup.sdk.models.Offer;

import java.util.List;

public interface OfferListRequestListener extends Request.ErrorListener {
    void onSuccess(List<Offer> offers);
}
