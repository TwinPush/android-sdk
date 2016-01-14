package com.yellowpineapple.wakup.sdk.activities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.models.Offer;
import com.yellowpineapple.wakup.sdk.utils.IntentBuilder;
import com.yellowpineapple.wakup.sdk.views.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SavedOffersActivity extends OfferListActivity {

    Date lastUpdate;

    StaggeredGridView gridView;
    PullToRefreshLayout ptrLayout;
    View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wk_activity_my_offers);
        injectViews();
    }

    private void injectViews() {
        ptrLayout = ((PullToRefreshLayout) findViewById(R.id.ptr_layout));
        emptyView = findViewById(R.id.emptyView);
        gridView = ((StaggeredGridView) findViewById(R.id.grid_view));
        afterViews();
    }

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

    // Builder

    public static Builder intent(Context context) {
        return new Builder(context);
    }

    public static class Builder extends IntentBuilder<SavedOffersActivity> {

        public Builder(Context context) {
            super(SavedOffersActivity.class, context);
        }
    }
}