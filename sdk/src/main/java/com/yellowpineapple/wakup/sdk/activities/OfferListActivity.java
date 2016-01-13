package com.yellowpineapple.wakup.sdk.activities;

import android.app.ActionBar;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.communications.Request;
import com.yellowpineapple.wakup.sdk.communications.requests.BaseRequest;
import com.yellowpineapple.wakup.sdk.communications.requests.OfferListRequestListener;
import com.yellowpineapple.wakup.sdk.controllers.OffersAdapter;
import com.yellowpineapple.wakup.sdk.models.Offer;
import com.yellowpineapple.wakup.sdk.views.PullToRefreshLayout;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by agutierrez on 09/02/15.
 */
public abstract class OfferListActivity extends ParentActivity implements AbsListView.OnScrollListener, OffersAdapter.Listener {

    OffersAdapter offersAdapter;
    boolean mHasRequestedMore;
    boolean mHasMoreResults = false;
    Request offersRequest = null;
    int offersPage = FIRST_PAGE;

    Location currentLocation = null;

    List<Offer> offers;
    Offer selectedOffer = null;

    static int FIRST_PAGE = BaseRequest.FIRST_PAGE;
    static int PER_PAGE = BaseRequest.RESULTS_PER_PAGE;

    AtomicInteger scrollPosition = new AtomicInteger(0);

    private StaggeredGridView gridView;
    private boolean hideActionBarOnScroll;
    ActionBar mActionBar;
    View navigationView;
    View emptyView;
    float actionBarHeight;

    interface AnimationListener {
        void onAnimationCompleted();
    }

    protected boolean shouldReloadOffers() {
        return offers == null;
    }

    void setupOffersGrid(StaggeredGridView gridView, View emptyView, final boolean hideActionBarOnScroll) {
        setupOffersGrid(gridView, null, emptyView, hideActionBarOnScroll);
    }

    void setupOffersGrid(StaggeredGridView gridView, View navigationView, View emptyView, final boolean hideActionBarOnScroll) {
        this.gridView = gridView;
        this.emptyView = emptyView;
        this.hideActionBarOnScroll = hideActionBarOnScroll;
        this.navigationView = navigationView;

        registerForContextMenu(gridView);

        if (getPullToRefreshLayout() != null) {
            setupPullToRefresh(getPullToRefreshLayout());
        }

        if (emptyView != null) emptyView.setVisibility(View.GONE);

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
            if (getPullToRefreshLayout() != null) {
                getPullToRefreshLayout().setProgressViewOffset(false,
                        Math.round(mActionBarHeight - actionBarHeight),
                        Math.round(mActionBarHeight + getResources().getDimension(R.dimen.pulltorefresh_margin)));
            }
        }

        offersAdapter = new OffersAdapter(this);
        offersAdapter.setListener(this);

        // do we have saved data?
        if (shouldReloadOffers()) reloadOffers();

        offersAdapter.setOffers(offers);

        gridView.setAdapter(offersAdapter);
        gridView.setOnScrollListener(this);
    }

    public PullToRefreshLayout getPullToRefreshLayout() {
        return null;
    }

    void setupPullToRefresh(PullToRefreshLayout pullToRefreshLayout) {
        pullToRefreshLayout.setOnRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadOffers();
            }
        });
        pullToRefreshLayout.setColorSchemeResources(R.color.secondary, R.color.primary);
        pullToRefreshLayout.setSwipeableChildren(gridView, emptyView);
    }

    protected void reloadOffers() {
        requestLoadPage(FIRST_PAGE);
    }

    protected void requestLoadPage(final int page) {
        this.offersPage = page;
        this.mHasRequestedMore = true;
        setLoading(true);
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
                setLoading(false);
                displayErrorDialog(exception);
            }
        });
    }

    @Override
    public void setLoading(boolean loading) {
        if (getPullToRefreshLayout() != null) {
            if (loading) {
                if (!getPullToRefreshLayout().isRefreshing()) {
                    setProgressBarIndeterminateVisibility(true);
                }
            } else {
                setProgressBarIndeterminateVisibility(false);
                getPullToRefreshLayout().setRefreshing(false);
            }
        } else {
            super.setLoading(loading);
        }
    }

    protected OfferListRequestListener getOfferListRequestListener() {
        return new OfferListRequestListener() {
            @Override
            public void onSuccess(List<Offer> offers) {
                setOffers(offersPage, offers);
            }

            @Override
            public void onError(Exception exception) {
                mHasRequestedMore = false;
                setLoading(false);
                displayErrorDialog(exception);
                offersRequest = null;
            }
        };
    }

    void setOffers(int page, List<Offer> newOffers) {
        mHasMoreResults = newOffers.size() >= PER_PAGE;
        if (page == FIRST_PAGE) {
            this.offers = newOffers;
        } else {
            this.offers.addAll(newOffers);
        }

        getOffersAdapter().setCurrentLocation(currentLocation);
        getOffersAdapter().setOffers(this.offers);
        getOffersAdapter().notifyDataSetChanged();
        mHasRequestedMore = false;
        setLoading(false);
        offersRequest = null;
        // Wearable offers are disabled for production
        if (page == FIRST_PAGE) {
            //showWearableOffers(offers);
        }
        setEmptyViewVisible(offers.size() == 0);
    }

    void showWearableOffers(List<Offer> offers) {
        // TODO Should be done in background
        //NotificationFactory.getInstance(OfferListActivity.this).showWearableOffers(offers, currentLocation);
    }

    /* Empty view */

    void setEmptyViewVisible(boolean visible) {
        if (emptyView != null) {
            emptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
            gridView.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
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

    void delayNavigationToggle(final boolean visible, final boolean animated, final AnimationListener listener) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleNavigationBarVisibility(visible, animated, listener);
            }
        }, 50);
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
    public void onOfferClick(Offer offer, View view) {
        showOfferDetail(offer, currentLocation);
    }

    @Override
    public void onOfferLongClick(Offer offer, View view) {
        this.selectedOffer = offer;
        openContextMenu(gridView);
    }

    abstract void onRequestOffers(final int page, final Location currentLocation);

    // Context menu

    enum OfferMenuItem {
        VIEW_IN_MAP(1, R.string.menu_map),
        MY_OFFERS_SAVE(2, R.string.menu_my_offers_add),
        MY_OFFERS_REMOVE(3, R.string.menu_my_offers_remove),
        SHARE(4, R.string.menu_share);

        int id;
        int textResId;

        private OfferMenuItem(int id, int textResId) {
            this.id = id;
            this.textResId = textResId;
        }

        public static OfferMenuItem fromId(int id) {
            for (OfferMenuItem item : values()) {
                if (item.getId() == id) {
                    return item;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }

        public int getTextResId() {
            return textResId;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == gridView) {
            if (selectedOffer.hasLocation()) {
                addMenuItem(menu, OfferMenuItem.VIEW_IN_MAP);
            }
            if (isSavedOffer(selectedOffer)) {
                addMenuItem(menu, OfferMenuItem.MY_OFFERS_REMOVE);
            } else {
                addMenuItem(menu, OfferMenuItem.MY_OFFERS_SAVE);
            }
            addMenuItem(menu, OfferMenuItem.SHARE);
        }
    }

    void addMenuItem(ContextMenu menu, OfferMenuItem menuItem) {
        menu.add(Menu.NONE, menuItem.getId(), Menu.NONE, menuItem.getTextResId());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        OfferMenuItem menuItem = OfferMenuItem.fromId(item.getItemId());
        switch (menuItem) {
            case VIEW_IN_MAP: displayInMap(selectedOffer, currentLocation); break;
            case MY_OFFERS_SAVE: saveOffer(selectedOffer); break;
            case MY_OFFERS_REMOVE: removeSavedOffer(selectedOffer); break;
            case SHARE: shareOffer(selectedOffer); break;
        }
        afterContextItemSelected(menuItem);
        return true;
    }

    protected void afterContextItemSelected(OfferMenuItem menuItem) {

    }

    public OffersAdapter getOffersAdapter() {
        return offersAdapter;
    }

    public List<Offer> getOffers() {
        return offers;
    }
}
