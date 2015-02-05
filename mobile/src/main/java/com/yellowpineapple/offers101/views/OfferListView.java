package com.yellowpineapple.offers101.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.Random;

@EViewGroup(R.layout.list_item_offer)
public class OfferListView extends FrameLayout {

    Offer offer;

    /* Views */
    @ViewById RemoteImageView offerImageView;
    @ViewById TextView txtCompany;
    @ViewById TextView txtDescription;
    @ViewById TextView txtShortOffer;
    @ViewById TextView txtDistance;
    @ViewById TextView txtExpiration;

    public OfferListView(Context context) {
        super(context);
        init(null, 0);
    }

    public OfferListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OfferListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

    }

    public void setOffer(Offer offer) {
        this.offer = offer;
        offerImageView.setImage(offer.getThumbnail());
        txtCompany.setText(offer.getCompany().getName());
        txtDescription.setText(offer.getShortDescription());
        txtShortOffer.setText(offer.getShortOffer());
        txtDistance.setText("a 20 m");
        int distance = new Random().nextInt(20);
        txtExpiration.setText(String.format(getResources().getText(R.string.offer_expires_in_x_days).toString(), distance));
    }
}
