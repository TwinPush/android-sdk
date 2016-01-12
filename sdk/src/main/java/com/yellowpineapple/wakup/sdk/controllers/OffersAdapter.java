package com.yellowpineapple.wakup.sdk.controllers;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yellowpineapple.wakup.sdk.models.Offer;
import com.yellowpineapple.wakup.sdk.views.OfferListView;

import java.util.List;

/***
 * ADAPTER
 */

public class OffersAdapter extends BaseAdapter implements View.OnLongClickListener, View.OnClickListener {

    List<Offer> offers;
    boolean loading;
    Context context;
    Location currentLocation;
    Listener listener;

    public OffersAdapter(final Context context) {
        super();
        this.context = context;
    }

    @Override
    public int getCount() {
        int count = 0;
        if (offers != null) {
            count = offers.size();
        }
        if (loading) {
            count++;
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        Offer offer = null;
        if (offers != null && position < offers.size()) {
            offer = offers.get(position);
        }
        return offer;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;
        if (!isLoadingView(position)) {
            final OfferListView offerView;
            if (convertView == null) {
                offerView = new OfferListView(getContext());
                offerView.setClickable(true);
                offerView.setLongClickable(true);
                offerView.setOnClickListener(this);
                offerView.setOnLongClickListener(this);
            } else {
                offerView = (OfferListView) convertView;
            }
            final Offer offer = offers.get(position);
            offerView.setOffer(offer, currentLocation);
            view = offerView;
        } else {
            TextView loadingView = new TextView(getContext());
            loadingView.setText("Loading...");
            view = loadingView;
        }

        return view;
    }

    boolean isLoadingView(int position) {
        int size = offers != null ? offers.size() : 0;
        return position == size;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof OfferListView) {
            OfferListView offerView = (OfferListView) v;
            if (listener != null) listener.onOfferClick(offerView.getOffer(), offerView);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v instanceof OfferListView) {
            OfferListView offerView = (OfferListView) v;
            if (listener != null) listener.onOfferLongClick(offerView.getOffer(), offerView);
            return true;
        }
        return false;
    }

    public interface Listener {
        void onOfferClick(Offer offer, View view);
        void onOfferLongClick(Offer offer, View view);
    }

    public Listener getListener() {
        return listener;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public Context getContext() {
        return context;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }
}