package com.yellowpineapple.wakup.activities;

import android.location.Location;
import android.view.View;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.models.Category;
import com.yellowpineapple.wakup.models.Offer;
import com.yellowpineapple.wakup.models.SearchResultItem;
import com.yellowpineapple.wakup.views.PullToRefreshLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(resName="activity_search_results")
public class SearchResultActivity extends OfferListActivity {

    @Extra SearchResultItem searchItem;
    @Extra List<Category> categories = null;

    /* Views */
    @ViewById StaggeredGridView gridView;
    @ViewById PullToRefreshLayout ptrLayout;
    @ViewById View emptyView;

    @AfterViews
    void afterViews() {
        setTitle(searchItem.getName());
        setupOffersGrid(gridView, emptyView, true);
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        switch (searchItem.getType()) {
            case COMPANY: {
                offersRequest = getRequestClient().findOffers(location, searchItem.getCompany(), categories, page, getOfferListRequestListener());
                break;
            }
            case NEAR_ME:
            case LOCATION: {
                offersRequest = getRequestClient().findOffers(searchItem.getLocation(), null, categories, page, getOfferListRequestListener());
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
        // Check that there is no category filter selected
        boolean fromStoreOffers = searchItem.getType() == SearchResultItem.Type.COMPANY &&
                (categories == null || categories.isEmpty());
        OfferDetailActivity_.intent(this).offer(offer).location(currentLocation).fromStoreOffers(fromStoreOffers).start();
        slideInTransition();
    }
}
