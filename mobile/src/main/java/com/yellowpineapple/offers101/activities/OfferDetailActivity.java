package com.yellowpineapple.offers101.activities;

import android.location.Location;
import android.os.Bundle;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.views.OfferDetailView;
import com.yellowpineapple.offers101.views.OfferDetailView_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_offers)
public class OfferDetailActivity extends OfferListActivity implements OfferDetailView.Listener {

    @Extra Offer offer;
    @Extra Location location;

    /* Views */
    @ViewById StaggeredGridView gridView;
    OfferDetailView offerDetailView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void afterViews() {
        if (offerDetailView == null) {
            offerDetailView = OfferDetailView_.build(this);
            offerDetailView.setListener(this);
            gridView.addHeaderView(offerDetailView);
        }
        offerDetailView.setOffer(offer, location);
        setupOffersGrid(gridView, true);
    }

    @OptionsItem(android.R.id.home)
    void onHomePressed() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        slideOutTransition();
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        offersRequest = getRequestClient().relatedOffers(offer, page, PER_PAGE, getOfferListRequestListener());
    }

    /* OfferDetailView.Listener */

    @Override
    public void onViewOnMapClicked(Offer offer) {
        OfferMapActivity_.intent(this).offer(offer).location(location).start();
        slideInTransition();
    }

    @Override
    public void onDescriptionClicked(Offer offer) {
        ModalTextActivity_.intent(this).text(offer.getDescription()).start();
    }
}
