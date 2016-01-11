package com.yellowpineapple.wakup.views;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.models.Offer;
import com.yellowpineapple.wakup.utils.PersistenceHandler;
import com.yellowpineapple.wakup.utils.Strings;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by agutierrez on 09/02/15.
 */
@EViewGroup(resName="view_offer_detail")
public class OfferDetailView extends LinearLayout {

    public interface Listener {
        void onViewOnMapClicked(Offer offer);
        void onSaveClicked(Offer offer);
        void onOpenLinkClicked(Offer offer);
        void onShareClicked(Offer offer);
        void onDescriptionClicked(Offer offer);
        void onStoreOffersClicked(Offer offer);
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
    @ViewById View storeOffersView;
    @ViewById TextView txtStoreOffers;

    @ViewById OfferActionButton btnWebsite;
    @ViewById OfferActionButton btnMap;
    @ViewById OfferActionButton btnSave;
    @ViewById OfferActionButton btnShare;

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

            // Offers count
            txtStoreOffers.setText(String.valueOf(offer.getCompany().getOfferCount()));
            storeOffersView.setVisibility(offer.getCompany().getOfferCount() > 1 ? VISIBLE : GONE);

            boolean hasLocation = offer.hasLocation();
            imgDisclosureAddress.setVisibility(hasLocation ? VISIBLE : GONE);
            storeView.setClickable(hasLocation);
            btnMap.setEnabled(hasLocation);
            btnMap.setVisibility(offer.isOnline() ? GONE : VISIBLE);
            btnWebsite.setVisibility(offer.isOnline() ? VISIBLE : GONE);
            // Disable Website Button until online offers have URL
            btnWebsite.setEnabled(false);
            refreshSavedState();
        }
    }

    void refreshSavedState() {
        boolean saved = PersistenceHandler.getSharedInstance(getContext()).isSavedOffer(offer);
        btnSave.setText(saved ? R.string.action_offer_saved : R.string.action_offer_save);
        btnSave.setSelected(saved);
    }

    @Click(resName="viewDescription")
    void onDescriptionClicked() {
        if (listener != null) listener.onDescriptionClicked(offer);
    }

    @Click(resName="storeView")
    void onAddressClicked() {
        if (listener != null) listener.onViewOnMapClicked(offer);
    }

    @Click
    void btnMap() { if (listener != null) listener.onViewOnMapClicked(offer); }

    @Click
    void btnWebsite() { if (listener != null) listener.onOpenLinkClicked(offer); }

    @Click
    void btnSave() {
        if (listener != null) listener.onSaveClicked(offer);
        refreshSavedState();
    }

    @Click
    void btnShare() { if (listener != null) listener.onShareClicked(offer); }

    @Click
    void storeOffersView() { if (listener != null) listener.onStoreOffersClicked(offer); }
}
