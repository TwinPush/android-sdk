package com.yellowpineapple.offers101.activities;

import android.location.Location;

import com.etsy.android.grid.StaggeredGridView;
import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.utils.Ln;
import com.yellowpineapple.offers101.utils.PersistenceHandler;
import com.yellowpineapple.offers101.views.OfferDetailView;
import com.yellowpineapple.offers101.views.OfferDetailView_;
import com.yellowpineapple.offers101.views.RelatedOffersHeader_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

@EActivity(R.layout.activity_offers_list)
public class OfferDetailActivity extends OfferListActivity implements OfferDetailView.Listener {

    @Extra Offer offer;
    @Extra Location location;

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
        OfferMapActivity_.intent(this).offer(offer).location(location).start();
        slideInTransition();
    }

    @Override
    public void onDescriptionClicked(Offer offer) {
        ModalTextActivity_.intent(this).text(offer.getDescription()).start();
    }

    @Override
    public void onSaveClicked(Offer offer) {
        PersistenceHandler persistence = getPersistence();
        if (persistence.isSavedOffer(offer)) {
            persistence.removeSavedOffer(offer);
        } else {
            persistence.saveOffer(offer);
        }
    }

    @Override
    public void onOpenLinkClicked(Offer offer) {
        Ln.i("Open link clicked");
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
        StoreOffersActivity_.intent(this).offer(offer).location(location).start();
    }
}
