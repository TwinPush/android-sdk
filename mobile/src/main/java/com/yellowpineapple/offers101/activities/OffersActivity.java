package com.yellowpineapple.offers101.activities;

import android.app.ActionBar;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.offers101.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

@EActivity(R.layout.activity_offers)
public class OffersActivity extends OfferListActivity {

    @ViewById StaggeredGridView gridView;
    @ViewById View navigationView;
    @ViewById PullToRefreshLayout ptrLayout;

    Date backPressedTime = null;

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
        setupOffersGrid(gridView, navigationView, true);
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

    @Click(R.id.btnBigOffer)
    void bigOfferPressed() {
        String url = "http://www.renault.es/gama-renault/renault-vehiculos-turismos/gama-kadjar/kadjar/?utm_campaign=ES-r-t-Kadjar-prelanzamiento_kadjar-Jan-2015";
        WebViewActivity_.intent(this).url(url).titleId(R.string.big_offer).start();
        slideInTransition();
    }

    @Click(R.id.btnMap)
    void mapButtonPressed() {
        OfferMapActivity_.intent(this).offers(offers).location(currentLocation).start();
        slideInTransition();
    }

    @Click(R.id.btnMyOffers)
    void myOffersPressed() {
        SavedOffersActivity_.intent(this).start();
        slideInTransition();
    }

    @Override
    public PullToRefreshLayout getPullToRefreshLayout() {
        return ptrLayout;
    }
}