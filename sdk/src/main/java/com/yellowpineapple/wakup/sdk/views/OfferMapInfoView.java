package com.yellowpineapple.wakup.sdk.views;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.models.Offer;

/**
 * Created by agutierrez on 09/02/15.
 */
public class OfferMapInfoView extends LinearLayout {

    Offer offer;
    /* Views */
    RemoteImageView imgCompany;
    TextView txtCompany;
    TextView txtAddress;
    TextView txtDistance;
    View imgDisclosure;

    public OfferMapInfoView(Context context) {
        super(context);
        init();
    }

    public OfferMapInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OfferMapInfoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        injectViews();
    }

    private void injectViews() {
        inflate(getContext(), R.layout.wk_view_map_offer_info, this);
        txtDistance = ((TextView) findViewById(R.id.txtDistance));
        txtCompany = ((TextView) findViewById(R.id.txtCompany));
        txtAddress = ((TextView) findViewById(R.id.txtAddress));
        imgDisclosure = findViewById(R.id.imgDisclosure);
        imgCompany = ((RemoteImageView) findViewById(R.id.imgCompany));
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
            imgDisclosure.setVisibility(isClickable() ? VISIBLE : GONE);
        }
    }
}
