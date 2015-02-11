package com.yellowpineapple.offers101.views;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.utils.Strings;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by agutierrez on 09/02/15.
 */
@EViewGroup(R.layout.view_offer_detail)
public class OfferDetailView extends LinearLayout {

    public interface Listener {
        void onViewOnMapClicked(Offer offer);
        void onDescriptionClicked(Offer offer);
    }

    Offer offer;
    @Setter @Getter Listener listener;

    /* Views */
    @ViewById RemoteImageView imgCompany;
    @ViewById TextView txtCompany;
    @ViewById TextView txtAddress;
    @ViewById TextView txtDistance;
    @ViewById RemoteImageView imgOffer;
    @ViewById TextView txtShortDescription;
    @ViewById TextView txtDescription;
    @ViewById TextView txtShortOffer;
    @ViewById TextView txtExpiration;
    @ViewById View viewDescription;
    @ViewById View storeView;
    @ViewById ImageView imgDisclosureAddress;

    public OfferDetailView(Context context) {
        super(context);
    }

    public OfferDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OfferDetailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOffer(Offer offer, Location location) {
        this.offer = offer;
        if (offer != null) {
            imgCompany.setImage(offer.getCompany().getLogo());
            imgOffer.setImage(offer.getImage(), offer.getThumbnail());
            txtCompany.setText(offer.getCompany().getName());
            txtDistance.setText(offer.getHumanizedDistance(getContext(), location));
            if (offer.getStore() != null) {
                txtAddress.setText(offer.getStore().getAddress());
                txtAddress.setVisibility(VISIBLE);
            } else {
                txtAddress.setVisibility(GONE);
            }
            viewDescription.setVisibility(Strings.isEmpty(offer.getDescription()) ? GONE : VISIBLE);
            txtDescription.setText(offer.getDescription());
            txtShortDescription.setText(offer.getShortDescription());
            txtShortOffer.setText(offer.getShortOffer());
            txtExpiration.setText(offer.getHumanizedExpiration(getContext()));

            boolean hasLocation = offer.hasLocation();
            imgDisclosureAddress.setVisibility(hasLocation ? VISIBLE : GONE);
            storeView.setClickable(hasLocation);

        }
    }

    @Click(R.id.viewDescription)
    void onDescriptionClicked() {
        if (listener != null) listener.onDescriptionClicked(offer);
    }

    @Click(R.id.storeView)
    void onAddressClicked() {
        if (listener != null) listener.onViewOnMapClicked(offer);
    }
}
