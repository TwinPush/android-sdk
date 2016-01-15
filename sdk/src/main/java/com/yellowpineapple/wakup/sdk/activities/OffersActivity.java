package com.yellowpineapple.wakup.sdk.activities;

import android.app.ActionBar;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.models.Offer;
import com.yellowpineapple.wakup.sdk.utils.IntentBuilder;
import com.yellowpineapple.wakup.sdk.views.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class OffersActivity extends OfferListActivity {

    StaggeredGridView gridView;
    View navigationView;
    PullToRefreshLayout ptrLayout;
    View emptyView;

    private static final String BIG_OFFER_URL = "http://app.wakup.net/offers/highlighted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wk_activity_offers);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        injectViews();
    }

    void injectViews() {
        emptyView = findViewById(R.id.emptyView);
        gridView = ((StaggeredGridView) findViewById(R.id.grid_view));
        navigationView = findViewById(R.id.navigationView);
        ptrLayout = ((PullToRefreshLayout) findViewById(R.id.ptr_layout));
        final View btnMap = findViewById(R.id.btnMap);
        final View btnBigOffer = findViewById(R.id.btnBigOffer);
        final View btnMyOffers = findViewById(R.id.btnMyOffers);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == btnMap) {
                    mapButtonPressed();
                } else if (view == btnBigOffer) {
                    bigOfferPressed();
                } else if (view == btnMyOffers) {
                    myOffersPressed();
                }
            }
        };

        View[] onClickViews = new View[] {
                btnMap, btnBigOffer, btnMyOffers
        };
        for (View view : onClickViews) {
            view.setOnClickListener(clickListener);
        }
        afterViews();
    }

    void afterViews() {
        setupOffersGrid(gridView, navigationView, emptyView, true);
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        offersRequest = getRequestClient().findOffers(location, page, getOfferListRequestListener());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    void bigOfferPressed() {
        WebViewActivity.intent(this).url(BIG_OFFER_URL).title(getString(R.string.wk_big_offer)).start();
        slideInTransition();
    }

    void mapButtonPressed() {
        int MAX_MAP_OFFERS = 20;
        List<Offer> mapOffers = new ArrayList<>(offers.subList(0, Math.min(MAX_MAP_OFFERS, offers.size())));
        OfferMapActivity.intent(this).offers(mapOffers).location(currentLocation).start();
        slideInTransition();
    }

    void myOffersPressed() {
        SavedOffersActivity.intent(this).start();
        slideInTransition();
    }

    @Override
    public PullToRefreshLayout getPullToRefreshLayout() {
        return ptrLayout;
    }

    void menuSearchSelected() {
        SearchActivity.intent(this).location(currentLocation).start();
        slideInTransition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.wk_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == R.id.menu_search) {
            menuSearchSelected();
            return true;
        }
        return false;
    }

    // Builder
    public static Builder intent(Context context) {
        return new Builder(context);
    }
    public static class Builder extends IntentBuilder<OffersActivity> {
        public Builder(Context context) {
            super(OffersActivity.class, context);
        }
    }
}