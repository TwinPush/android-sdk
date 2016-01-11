package com.yellowpineapple.wakup.activities;

import android.app.ActionBar;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.views.PullToRefreshLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

@EActivity(resName="activity_offers")
@OptionsMenu(resName="main_menu")
public class OffersActivity extends OfferListActivity {

    @ViewById StaggeredGridView gridView;
    @ViewById View navigationView;
    @ViewById PullToRefreshLayout ptrLayout;
    @ViewById View emptyView;

    Date backPressedTime = null;

    private static final String BIG_OFFER_URL = "http://app.wakup.net/offers/highlighted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @AfterViews
    void afterViews() {
        setupOffersGrid(gridView, navigationView, emptyView, true);
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        offersRequest = getRequestClient().findOffers(location, page, getOfferListRequestListener());
    }

    @Override
    public void onBackPressed() {
        long diff = backPressedTime != null ? new Date().getTime() - backPressedTime.getTime(): Long.MAX_VALUE;
        float secondsDiff = diff / 1000;
        if (secondsDiff > 0.5 && secondsDiff < 3) {
            finish();
        } else {
            backPressedTime = new Date();
            Toast.makeText(this, R.string.back_button_once, Toast.LENGTH_SHORT).show();
        }
    }

    @Click(resName="btnBigOffer")
    void bigOfferPressed() {
        WebViewActivity_.intent(this).url(BIG_OFFER_URL).titleId(R.string.big_offer).start();
        slideInTransition();
    }

    @Click(resName="btnMap")
    void mapButtonPressed() {
        OfferMapActivity_.intent(this).offers(offers).location(currentLocation).start();
        slideInTransition();
    }

    @Click(resName="btnMyOffers")
    void myOffersPressed() {
        SavedOffersActivity_.intent(this).start();
        slideInTransition();
    }

    @Override
    public PullToRefreshLayout getPullToRefreshLayout() {
        return ptrLayout;
    }

    @OptionsItem
    void menuSearchSelected() {
        SearchActivity_.intent(this).location(currentLocation).start();
        slideInTransition();
    }
}