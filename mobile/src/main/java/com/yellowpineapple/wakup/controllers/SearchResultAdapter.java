package com.yellowpineapple.wakup.controllers;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.models.Company;
import com.yellowpineapple.wakup.models.SearchResultItem;
import com.yellowpineapple.wakup.views.SearchItemView;
import com.yellowpineapple.wakup.views.SearchItemView_;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;

/***
 * ADAPTER
 */

public class SearchResultAdapter extends BaseAdapter implements View.OnClickListener {

    @Getter List<Company> companies = null;
    @Getter List<Address> addresses = null;
    @Getter Context context;
    Location currentLocation;

    @Getter @Setter Listener listener;
    @Getter List<SearchResultItem> resultItems = new ArrayList<>();

    public interface Listener {
        void onItemClick(SearchResultItem item, View view);
    }

    public SearchResultAdapter(final Context context, Location currentLocation) {
        super();
        this.context = context;
        this.currentLocation = currentLocation;
        refreshResultItems();
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
        if (items.isEmpty() && currentLocation != null) {
            Address address = new Address(Locale.getDefault());
            address.setFeatureName(context.getText(R.string.near_me).toString());
            address.setLatitude(currentLocation.getLatitude());
            address.setLongitude(currentLocation.getLongitude());

            items.add(new SearchResultItem(false, address));
        }
        this.resultItems = items;
    }

    @Override
    public int getCount() {
        return getResultItems().size();
    }

    @Override
    public SearchResultItem getItem(int position) {
        SearchResultItem item = null;
        if (position < resultItems.size()) {
            item = resultItems.get(position);
        }
        return item;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final SearchItemView itemView;
        if (convertView == null) {
            itemView = SearchItemView_.build(context);
        } else {
            itemView = (SearchItemView) convertView;
        }
        final SearchResultItem searchItem = getItem(position);

        itemView.setClickable(true);
        itemView.setOnClickListener(this);

        itemView.setSearchItem(searchItem);
        return itemView;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof SearchItemView) {
            SearchItemView view = (SearchItemView) v;
            if (listener != null) listener.onItemClick(view.getSearchItem(), view);
        }
    }
}