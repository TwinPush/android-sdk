package com.yellowpineapple.wakup.controllers;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yellowpineapple.wakup.models.Company;
import com.yellowpineapple.wakup.models.Offer;
import com.yellowpineapple.wakup.models.SearchResultItem;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/***
 * ADAPTER
 */

public class SearchResultAdapter extends BaseAdapter implements View.OnLongClickListener, View.OnClickListener {

    @Getter List<Company> companies;
    @Getter List<Address> addresses;
    @Getter Context context;
    @Setter Location currentLocation;

    @Getter @Setter Listener listener;
    @Getter List<SearchResultItem> resultItems = new ArrayList<>();

    public interface Listener {
        void onOfferClick(Offer offer, View view);
        void onOfferLongClick(Offer offer, View view);
    }

    public SearchResultAdapter(final Context context) {
        super();
        this.context = context;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
        refreshResultItems();
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
        refreshResultItems();
    }

    private void refreshResultItems() {
        List<SearchResultItem> items = new ArrayList<>();
        if (companies != null) {
            for (Company company : companies) {
                items.add(new SearchResultItem(false, company));
            }
        }
        if (addresses != null) {
            for (Address address : addresses) {
                items.add(new SearchResultItem(false, address));
            }
        }
        this.resultItems = items;
    }

    @Override
    public int getCount() {
        return getResultItems().size();
    }

    @Override
    public SearchResultItem getItem(int position) {
        return getResultItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view;
        final TextView itemView;
        if (convertView == null) {
            itemView = new TextView(context);
        } else {
            itemView = (TextView) convertView;
        }
        final String item = getItem(position).getName();
        itemView.setText(item);
        view = itemView;

        return view;
    }

    @Override
    public void onClick(View v) {
//        if (v instanceof TextView) {
//            TextView view = (TextView) v;
//            if (listener != null) listener.onOfferClick(view.getOffer(), offerView);
//        }
    }

    @Override
    public boolean onLongClick(View v) {
//        if (v instanceof OfferListView) {
//            OfferListView offerView = (OfferListView) v;
//            if (listener != null) listener.onOfferLongClick(offerView.getOffer(), offerView);
//            return true;
//        }
        return false;
    }
}