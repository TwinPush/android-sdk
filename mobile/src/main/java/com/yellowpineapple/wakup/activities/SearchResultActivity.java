package com.yellowpineapple.wakup.activities;

import android.location.Location;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.models.Offer;
import com.yellowpineapple.wakup.models.SearchResultItem;
import com.yellowpineapple.wakup.views.PullToRefreshLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_offers_list)
public class SearchResultActivity extends OfferListActivity {

    @Extra SearchResultItem searchItem;

    /* Views */
    @ViewById StaggeredGridView gridView;
    @ViewById PullToRefreshLayout ptrLayout;

    @AfterViews
    void afterViews() {
        setTitle(searchItem.getName());
        setupOffersGrid(gridView, null, true);
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        switch (searchItem.getType()) {
            case COMPANY: {
                offersRequest = getRequestClient().findOffers(location, searchItem.getCompany(), page, getOfferListRequestListener());
                break;
            }
            case LOCATION: {
                offersRequest = getRequestClient().findOffers(searchItem.getLocation(), page, getOfferListRequestListener());
                // Display offers as if the user was in the requested location
                currentLocation = searchItem.getLocation();
                break;
            }
        }

    }

    @Override
    public PullToRefreshLayout getPullToRefreshLayout() {
        return ptrLayout;
    }

    protected void showOfferDetail(Offer offer, Location currentLocation) {
        OfferDetailActivity_.intent(this).offer(offer).location(currentLocation).start();
        slideInTransition();
    }
}
