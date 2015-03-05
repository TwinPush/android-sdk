package com.yellowpineapple.offers101.activities;

import android.location.Location;
import android.view.View;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.views.PullToRefreshLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EActivity(R.layout.activity_my_offers)
public class SavedOffersActivity extends OfferListActivity {

    Date lastUpdate;

    @ViewById StaggeredGridView gridView;
    @ViewById
    PullToRefreshLayout ptrLayout;
    @ViewById View emptyView;

    @AfterViews
    void afterViews() {
        setupOffersGrid(gridView, null, emptyView, true);
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        if (page == FIRST_PAGE) lastUpdate = new Date();
        List<String> userOffers = getPersistence().getUserOffers();
        if (userOffers.size() > 0) {
            offersRequest = getRequestClient().findOffersById(userOffers, location, page, getOfferListRequestListener());
        } else {
            setEmptyViewVisible(true);
            getOfferListRequestListener().onSuccess(new ArrayList<Offer>());
        }
    }

    @Override
    protected boolean shouldReloadOffers() {
        return offers == null || lastUpdate == null || getPersistence().userOffersChanged(lastUpdate);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (lastUpdate != null && shouldReloadOffers()) {
            reloadOffers();
        }
    }

    @Override
    public PullToRefreshLayout getPullToRefreshLayout() {
        return ptrLayout;
    }

    @Override
    protected void afterContextItemSelected(OfferMenuItem menuItem) {
        // Reload offers after removing from my offers to refresh displayed data
        if (menuItem == OfferMenuItem.MY_OFFERS_REMOVE) {
            reloadOffers();
        }
    }
}