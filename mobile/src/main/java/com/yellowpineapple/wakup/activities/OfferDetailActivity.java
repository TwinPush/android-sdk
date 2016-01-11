package com.yellowpineapple.wakup.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.models.Offer;
import com.yellowpineapple.wakup.utils.IntentBuilder;
import com.yellowpineapple.wakup.utils.PersistenceHandler;
import com.yellowpineapple.wakup.views.OfferDetailView;
import com.yellowpineapple.wakup.views.OfferDetailView_;
import com.yellowpineapple.wakup.views.PullToRefreshLayout;
import com.yellowpineapple.wakup.views.RelatedOffersHeader_;

public class OfferDetailActivity extends OfferListActivity implements OfferDetailView.Listener {

    public final static String OFFER_EXTRA = "offer";
    public final static String LOCATION_EXTRA = "location";
    public final static String FROM_STORE_OFFERS_EXTRA = "fromStoreOffers";
    Offer offer;
    Location location;
    boolean fromStoreOffers = false;

    /* Views */
    StaggeredGridView gridView;
    PullToRefreshLayout ptrLayout;
    OfferDetailView offerDetailView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_list);
        injectExtras();
        injectViews();

        if (offerDetailView == null) {
            offerDetailView = OfferDetailView_.build(this);
            offerDetailView.setListener(this);
            gridView.addHeaderView(offerDetailView);
            gridView.addHeaderView(RelatedOffersHeader_.build(this));
        }
        setTitle(offer.getCompany().getName());
        offerDetailView.setOffer(offer, location);
        setupOffersGrid(gridView, null, true);
    }

    private void injectExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras!= null) {
            if (extras.containsKey(OFFER_EXTRA)) {
                offer = ((Offer) extras.getSerializable(OFFER_EXTRA));
            }
            if (extras.containsKey(LOCATION_EXTRA)) {
                location = extras.getParcelable(LOCATION_EXTRA);
            }
            if (extras.containsKey(FROM_STORE_OFFERS_EXTRA)) {
                fromStoreOffers = extras.getBoolean(FROM_STORE_OFFERS_EXTRA);
            }
        }
    }

    private void injectViews() {
        gridView = (StaggeredGridView) findViewById(com.yellowpineapple.wakup.R.id.grid_view);
        ptrLayout = (PullToRefreshLayout) findViewById(com.yellowpineapple.wakup.R.id.ptr_layout);
    }

    @Override
    void onRequestOffers(final int page, final Location location) {
        offersRequest = getRequestClient().relatedOffers(offer, page, PER_PAGE, getOfferListRequestListener());
    }

    /* OfferDetailView.Listener */

    @Override
    public void onViewOnMapClicked(Offer offer) {
        displayInMap(offer, currentLocation);
    }

    @Override
    public void onDescriptionClicked(Offer offer) {
        ModalTextActivity.intent(this, offer.getDescription()).start();
    }

    @Override
    public void onSaveClicked(Offer offer) {
        PersistenceHandler persistence = getPersistence();
        if (persistence.isSavedOffer(offer)) {
            removeSavedOffer(offer);
        } else {
            saveOffer(offer);
        }
    }

    @Override
    public void onOpenLinkClicked(Offer offer) {
        openOfferLink(offer);
    }

    @Override
    public void onShareClicked(Offer offer) {
        shareOffer(offer);
    }

    @Override
    public PullToRefreshLayout getPullToRefreshLayout() {
        return ptrLayout;
    }

    @Override
    public void onStoreOffersClicked(Offer offer) {
        if (fromStoreOffers) {
            onBackPressed();
        } else {
            StoreOffersActivity_.intent(this).offer(offer).location(location).start();
            slideInTransition();
        }
    }

    // Builder

    public static Builder intent(Context context) {
        return new Builder(context);
    }

    public static class Builder extends IntentBuilder<OfferDetailActivity> {

        public Builder(Context context) {
            super(OfferDetailActivity.class, context);
        }

        public Builder offer(Offer offer) {
            getIntent().putExtra(OFFER_EXTRA, offer);
            return this;
        }

        public Builder location(Location location) {
            getIntent().putExtra(LOCATION_EXTRA, location);
            return this;
        }

        public Builder fromStoreOffers(boolean fromStoreOffers) {
            getIntent().putExtra(FROM_STORE_OFFERS_EXTRA, fromStoreOffers);
            return this;
        }

    }
}
