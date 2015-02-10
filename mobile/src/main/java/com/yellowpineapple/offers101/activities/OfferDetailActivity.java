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

@EActivity(R.layout.activity_offer_detail)
public class OfferDetailActivity extends OfferListActivity {

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
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right);
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        offersRequest = getRequestClient().relatedOffers(offer, page, PER_PAGE, getOfferListRequestListener());
    }
}
