package com.yellowpineapple.wakup.activities;

import android.location.Location;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.models.Offer;
import com.yellowpineapple.wakup.utils.PersistenceHandler;
import com.yellowpineapple.wakup.views.OfferDetailView;
import com.yellowpineapple.wakup.views.OfferDetailView_;
import com.yellowpineapple.wakup.views.PullToRefreshLayout;
import com.yellowpineapple.wakup.views.RelatedOffersHeader_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_offers_list)
public class OfferDetailActivity extends OfferListActivity implements OfferDetailView.Listener {

    @Extra Offer offer;
    @Extra Location location;
    @Extra boolean fromStoreOffers = false;

    /* Views */
    @ViewById StaggeredGridView gridView;
    @ViewById PullToRefreshLayout ptrLayout;
    OfferDetailView offerDetailView = null;

    @AfterViews
    void afterViews() {
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
        ModalTextActivity_.intent(this).text(offer.getDescription()).start();
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
}
