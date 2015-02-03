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
import com.yellowpineapple.offers101.utils.Ln;

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

    private ArrayList<String> mData;

    @ViewById StaggeredGridView gridView;

    @AfterViews
    void afterViews() {
        setTitle("101 Offers");

        mAdapter = new OffersAdapter(this, R.id.txt_line1);

        // do we have saved data?
        if (mData == null) {
            mData = generateSampleData();
        }

        for (String data : mData) {
            mAdapter.add(data);
        }

        gridView.setAdapter(mAdapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);

        loadOffers(0);
    }

    @OptionsItem
    void actionSearch() {
        Toast.makeText(this, "Search menu selected", Toast.LENGTH_SHORT).show();
        loadOffers(0);
    }

    void loadOffers(int page) {
        displayLoadingDialog();
        getLastKnownLocation(new LocationListener() {
            @Override
            public void onLocationSuccess(Location location) {
                getRequestClient().findOffers(location, new OfferListRequestListener() {
                    @Override
                    public void onSuccess(List<Offer> offers) {
                        String message = String.format("Obtained %d offers", offers.size());
                        Toast.makeText(OffersActivity.this, message, Toast.LENGTH_LONG).show();
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
        if (!mHasRequestedMore) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen >= totalItemCount) {
                Ln.d("onScroll lastInScreen - so load more");
                mHasRequestedMore = true;
                onLoadMoreItems();
            }
        }
    }

    private void onLoadMoreItems() {
        final ArrayList<String> sampleData = generateSampleData();
        for (String data : sampleData) {
            mAdapter.add(data);
        }
        // stash all the data in our backing store
        mData.addAll(sampleData);
        // notify the adapter that we can update now
        mAdapter.notifyDataSetChanged();
        mHasRequestedMore = false;
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

    public static final int SAMPLE_DATA_ITEM_COUNT = 30;
    public ArrayList<String> generateSampleData() {
        final ArrayList<String> data = new ArrayList<>(SAMPLE_DATA_ITEM_COUNT);

        for (int i = 0; i < SAMPLE_DATA_ITEM_COUNT; i++) {
            data.add("SAMPLE #");
        }

        return data;
    }
}