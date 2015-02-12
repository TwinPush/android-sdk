package com.yellowpineapple.offers101.activities;

import android.app.ActionBar;
import android.content.res.TypedArray;
import android.location.Location;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.communications.Request;
import com.yellowpineapple.offers101.communications.requests.OfferListRequestListener;
import com.yellowpineapple.offers101.controllers.OffersAdapter;
import com.yellowpineapple.offers101.models.Offer;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

/**
 * Created by agutierrez on 09/02/15.
 */
@EActivity
public abstract class OfferListActivity extends ParentActivity implements AbsListView.OnScrollListener, AbsListView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    @Getter OffersAdapter offersAdapter;
    boolean mHasRequestedMore;
    boolean mHasMoreResults = false;
    Request offersRequest = null;
    int offersPage = FIRST_PAGE;

    Location currentLocation = null;

    @Getter List<Offer> offers;

    static int FIRST_PAGE = 0;
    static int PER_PAGE = 30;

    AtomicInteger scrollPosition = new AtomicInteger(0);

    private StaggeredGridView gridView;
    private boolean hideActionBarOnScroll;
    ActionBar mActionBar;
    View navigationView;
    float actionBarHeight;

    interface AnimationListener {
        void onAnimationCompleted();
    }

    void setupOffersGrid(StaggeredGridView gridView, final boolean hideActionBarOnScroll) {
        setupOffersGrid(gridView, null, hideActionBarOnScroll);
    }

    void setupOffersGrid(StaggeredGridView gridView, View navigationView, final boolean hideActionBarOnScroll) {
        this.gridView = gridView;
        this.hideActionBarOnScroll = hideActionBarOnScroll;
        this.navigationView = navigationView;

        offersAdapter = new OffersAdapter(this);

        // do we have saved data?
        if (offers == null) {
            offers = new ArrayList<>();
            reloadOffers();
        }

        offersAdapter.setOffers(offers);

        gridView.setAdapter(offersAdapter);
        gridView.setOnScrollListener(this);
        gridView.setOnItemClickListener(this);
        gridView.setOnItemLongClickListener(this);

        if (hideActionBarOnScroll) {
            final TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
            float mActionBarHeight = styledAttributes.getDimension(0, 0);
            this.actionBarHeight = mActionBarHeight;
            if (navigationView != null) {
                mActionBarHeight *= 2;
            }
            styledAttributes.recycle();

            mActionBar = getActionBar();

            gridView.setPadding(gridView.getPaddingLeft(), Math.round(mActionBarHeight), gridView.getPaddingRight(), gridView.getPaddingBottom());
        }
    }

    protected void reloadOffers() {
        requestLoadPage(FIRST_PAGE);
    }

    protected void requestLoadPage(final int page) {
        this.offersPage = page;
        this.mHasRequestedMore = true;
        displayLoadingDialog();
        if (offersRequest != null) {
            offersRequest.cancel();
        }
        getLastKnownLocation(new LocationListener() {
            @Override
            public void onLocationSuccess(final Location location) {
                currentLocation = location;
                onRequestOffers(page, location);
            }

            @Override
            public void onLocationError(Exception exception) {
                mHasRequestedMore = false;
                displayErrorDialog(exception);
            }
        });
    }

    protected OfferListRequestListener getOfferListRequestListener() {
        return new OfferListRequestListener() {
            @Override
            public void onSuccess(List<Offer> offers) {
                mHasMoreResults = offers.size() >= PER_PAGE;
                OfferListActivity.this.offers.addAll(offers);
                getOffersAdapter().setCurrentLocation(currentLocation);
                getOffersAdapter().notifyDataSetChanged();
                mHasRequestedMore = false;
                closeLoadingDialog();
                offersRequest = null;
            }

            @Override
            public void onError(Exception exception) {
                mHasRequestedMore = false;
                displayErrorDialog(exception);
                offersRequest = null;
            }
        };
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
        if (hideActionBarOnScroll) {
            // Only compare left column to avoid ActionBar show/hide jumps
            if (firstVisibleItem == 0 || (firstVisibleItem - gridView.getHeaderViewsCount()) % 2 == 0) {
                int compare = new Integer(firstVisibleItem).compareTo(scrollPosition.getAndSet(firstVisibleItem));
                if (compare > 0) {
                    if (mActionBar.isShowing()) {
                        toggleNavigationBarVisibility(false, true, new AnimationListener() {
                            @Override
                            public void onAnimationCompleted() {
                                mActionBar.hide();
                            }
                        });
                    }

                } else if (compare < 0) {
                    if (!mActionBar.isShowing()) {
                        mActionBar.show();
                        delayNavigationToggle(true, true, null);
                    }
                }
            }
        }
    }

    @UiThread(delay = 50)
    void delayNavigationToggle(boolean visible, boolean animated, final AnimationListener listener) {
        toggleNavigationBarVisibility(visible, animated, listener);
    }

    private void toggleNavigationBarVisibility(final boolean visible, boolean animated, final AnimationListener listener) {
        if (navigationView != null) {
            if (animated) {
                Animation a = AnimationUtils.loadAnimation(this, visible ? R.anim.slide_in_down : R.anim.slide_out_up);
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        navigationView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (!visible) navigationView.setVisibility(View.GONE);
                        if (listener != null) listener.onAnimationCompleted();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                navigationView.startAnimation(a);
            } else {
                navigationView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
        if (listener != null) listener.onAnimationCompleted();
    }

    private void onLoadMoreItems() {
        // notify the adapter that we can update now
        mHasRequestedMore = false;
        requestLoadPage(offersPage + 1);
    }

    @Override
    protected void onResume() {
        getActionBar().show();
        toggleNavigationBarVisibility(true, false, null);
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        int offerPosition = position - gridView.getHeaderViewsCount();
        if (offerPosition >= 0) {
            OfferDetailActivity_.intent(this).offer(offers.get(offerPosition)).location(currentLocation).start();
            slideInTransition();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        Toast.makeText(this, "Item Long Clicked: " + position, Toast.LENGTH_SHORT).show();
        return true;
    }

    abstract void onRequestOffers(final int page, final Location location);

    View getNavigationView() {
        return null;
    }
}
