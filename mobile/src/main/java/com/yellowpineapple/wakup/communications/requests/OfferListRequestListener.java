package com.yellowpineapple.wakup.communications.requests;

import com.yellowpineapple.wakup.communications.Request;
import com.yellowpineapple.wakup.models.Offer;

import java.util.List;

public interface OfferListRequestListener extends Request.ErrorListener {
    void onSuccess(List<Offer> offers);
}
