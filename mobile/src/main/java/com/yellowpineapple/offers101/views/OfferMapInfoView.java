package com.yellowpineapple.offers101.views;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by agutierrez on 09/02/15.
 */
@EViewGroup(R.layout.view_map_offer_info)
public class OfferMapInfoView extends LinearLayout {

    Offer offer;

    /* Views */
    @ViewById RemoteImageView imgCompany;
    @ViewById TextView txtCompany;
    @ViewById TextView txtAddress;
    @ViewById TextView txtDistance;

    public OfferMapInfoView(Context context) {
        super(context);
    }

    public OfferMapInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OfferMapInfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOffer(Offer offer, Location location) {
        this.offer = offer;
        if (offer != null) {
            txtCompany.setText(offer.getCompany().getName());
            // Load company logo synchronously to ensure that
            // it's loaded when info window is displayed
            imgCompany.setImageSync(offer.getCompany().getLogo());
            if (offer.getStore() != null) {
                txtAddress.setText(offer.getStore().getAddress());
                txtAddress.setVisibility(VISIBLE);
            } else {
                txtAddress.setVisibility(GONE);
            }
            txtDistance.setText(offer.getHumanizedDistance(getContext(), location));
        }
    }
}
