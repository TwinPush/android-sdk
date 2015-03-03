package com.yellowpineapple.offers101.activities;

import android.location.Location;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.views.PullToRefreshLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_offers_list)
public class StoreOffersActivity extends OfferListActivity {

    @Extra Offer offer;
    @Extra Location location;

    /* Views */
    @ViewById StaggeredGridView gridView;
    @ViewById PullToRefreshLayout ptrLayout;

    @AfterViews
    void afterViews() {
        setTitle(String.format(getString(R.string.store_offers_title), offer.getCompany().getName()));
        setupOffersGrid(gridView, null, true);
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        offersRequest = getRequestClient().getCompanyOffers(offer.getCompany(), offer.getStore(), page, getOfferListRequestListener());
    }

    @Override
    public PullToRefreshLayout getPullToRefreshLayout() {
        return ptrLayout;
    }

    protected void showOfferDetailActivity(Offer offer, Location currentLocation) {
        OfferDetailActivity_.intent(this).offer(offer).location(currentLocation).fromStoreOffers(true).start();
        slideInTransition();
    }
}
