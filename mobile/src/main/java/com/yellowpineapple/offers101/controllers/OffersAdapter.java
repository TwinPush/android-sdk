package com.yellowpineapple.offers101.controllers;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.views.OfferListView;
import com.yellowpineapple.offers101.views.OfferListView_;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/***
 * ADAPTER
 */

public class OffersAdapter extends BaseAdapter {

    @Getter @Setter List<Offer> offers;
    @Getter @Setter boolean loading;
    @Getter Context context;
    @Setter Location currentLocation;

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
            OfferListView offerView;
            if (convertView == null) {
                offerView = OfferListView_.build(getContext());
            } else {
                offerView = (OfferListView) convertView;
            }
            offerView.setOffer(offers.get(position), currentLocation);
            view = offerView;
        } else {
            TextView loadingView = new TextView(getContext());
            loadingView.setText("loading...");
            view = loadingView;
        }

        return view;
    }

    boolean isLoadingView(int position) {
        int size = offers != null ? offers.size() : 0;
        return position == size;
    }
}