package com.yellowpineapple.offers101.activities;

import android.location.Location;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.offers101.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

@OptionsMenu(R.menu.menu_offers)
@EActivity(R.layout.activity_offers)
public class OffersActivity extends OfferListActivity {

    @ViewById StaggeredGridView gridView;

    @AfterViews
    void afterViews() {
        setupOffersGrid(gridView, true);
    }

    @OptionsItem
    void actionSearch() {
        Toast.makeText(this, "Search menu selected", Toast.LENGTH_SHORT).show();
        reloadOffers();
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        offersRequest = getRequestClient().findOffers(location, page, PER_PAGE, getOfferListRequestListener());
    }
}