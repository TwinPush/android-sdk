package com.yellowpineapple.wakup.sdk.activities;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.models.Category;
import com.yellowpineapple.wakup.sdk.models.Offer;
import com.yellowpineapple.wakup.sdk.models.SearchResultItem;
import com.yellowpineapple.wakup.sdk.utils.IntentBuilder;
import com.yellowpineapple.wakup.sdk.views.PullToRefreshLayout;

import java.io.Serializable;
import java.util.List;

public class SearchResultActivity extends OfferListActivity {

    public final static String CATEGORIES_EXTRA = "categories";
    public final static String SEARCH_ITEM_EXTRA = "searchItem";
    SearchResultItem searchItem;
    List<Category> categories = null;

    /* Views */
    StaggeredGridView gridView;
    PullToRefreshLayout ptrLayout;
    View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        injectExtras();
        injectViews();
    }

    private void injectViews() {
        ptrLayout = ((PullToRefreshLayout) findViewById(R.id.ptr_layout));
        gridView = ((StaggeredGridView) findViewById(R.id.grid_view));
        emptyView = findViewById(R.id.emptyView);
        afterViews();
    }

    @SuppressWarnings("unchecked")
    private void injectExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras!= null) {
            if (extras.containsKey(CATEGORIES_EXTRA)) {
                categories = ((List<Category> ) extras.getSerializable(CATEGORIES_EXTRA));
            }
            if (extras.containsKey(SEARCH_ITEM_EXTRA)) {
                searchItem = ((SearchResultItem) extras.getSerializable(SEARCH_ITEM_EXTRA));
            }
        }
    }

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
        OfferDetailActivity.intent(this).offer(offer).location(currentLocation).fromStoreOffers(fromStoreOffers).start();
        slideInTransition();
    }

    // Builder

    public static Builder intent(Context context) {
        return new Builder(context);
    }

    public static class Builder extends IntentBuilder<SearchResultActivity> {

        public Builder(Context context) {
            super(SearchResultActivity.class, context);
        }

        public Builder categories(List<Category> categories) {
            getIntent().putExtra(CATEGORIES_EXTRA, (Serializable) categories);
            return this;
        }

        public Builder searchItem(SearchResultItem searchItem) {
            getIntent().putExtra(SEARCH_ITEM_EXTRA, searchItem);
            return this;
        }
    }
}
