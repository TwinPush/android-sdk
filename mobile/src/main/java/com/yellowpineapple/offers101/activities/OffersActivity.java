package com.yellowpineapple.offers101.activities;

import android.location.Location;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.communications.requests.OfferListRequestListener;
import com.yellowpineapple.offers101.controllers.OffersAdapter;
import com.yellowpineapple.offers101.models.Offer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@OptionsMenu(R.menu.menu_offers)
@EActivity(R.layout.activity_offers)
public class OffersActivity extends ParentActivity implements AbsListView.OnScrollListener, AbsListView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private boolean mHasRequestedMore;
    private OffersAdapter mAdapter;
    private boolean mHasMoreResults = false;

    private List<Offer> offers;
    private static int FIRST_PAGE = 0;
    private int offersPage = FIRST_PAGE;

    @ViewById StaggeredGridView gridView;

    @AfterViews
    void afterViews() {
        mAdapter = new OffersAdapter(this);

        // do we have saved data?
        if (offers == null) {
            offers = new ArrayList<>();
            loadOffers(FIRST_PAGE);
        }

        mAdapter.setOffers(offers);

        gridView.setAdapter(mAdapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);
    }

    @OptionsItem
    void actionSearch() {
        Toast.makeText(this, "Search menu selected", Toast.LENGTH_SHORT).show();
        loadOffers(FIRST_PAGE);
    }

    void loadOffers(int page) {
        this.offersPage = page;
        this.mHasRequestedMore = true;
        displayLoadingDialog();
        getLastKnownLocation(new LocationListener() {
            @Override
            public void onLocationSuccess(Location location) {
                getRequestClient().findOffers(location, new OfferListRequestListener() {
                    @Override
                    public void onSuccess(List<Offer> offers) {
                        OffersActivity.this.offers.addAll(offers);
                        mAdapter.notifyDataSetChanged();
                        closeLoadingDialog();
                    }

                    @Override
                    public void onError(Exception exception) {
                        displayErrorDialog(exception);
                    }
                });
            }

            @Override
            public void onLocationError(Exception exception) {
                displayErrorDialog(exception);
            }
        });
    }

    /* Scroll Events */

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {}

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        if (!mHasRequestedMore && mHasMoreResults) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen >= totalItemCount) {
                onLoadMoreItems();
            }
        }
    }

    private void onLoadMoreItems() {
        // notify the adapter that we can update now
        mHasRequestedMore = false;
        loadOffers(offersPage + 1);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        OfferDetailActivity_.intent(this).start();
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Toast.makeText(this, "Item Long Clicked: " + position, Toast.LENGTH_SHORT).show();
        return true;
    }
}