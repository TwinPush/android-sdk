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
import com.yellowpineapple.wakup.views.SearchHeaderView;
import com.yellowpineapple.wakup.views.SearchHeaderView_;
import com.yellowpineapple.wakup.views.SearchItemView;
import com.yellowpineapple.wakup.views.SearchItemView_;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/***
 * ADAPTER
 */

public class SearchResultAdapter extends BaseAdapter implements View.OnClickListener {

    List<Company> companies = null;
    List<Address> addresses = null;
    Context context;
    Location currentLocation;

    Listener listener;
    List<SearchResultItem> recentSearches = new ArrayList<>();
    List<SearchResultItem> resultItems = new ArrayList<>();

    public interface Listener {
        void onItemClick(SearchResultItem item, View view);
    }

    public SearchResultAdapter(final Context context, Location currentLocation, List<SearchResultItem> recentSearches) {
        super();
        this.context = context;
        this.currentLocation = currentLocation;
        this.recentSearches = recentSearches;
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
        if (companies != null && !companies.isEmpty()) {
            items.add(new SearchResultItem(context.getString(R.string.search_brands)));
            for (Company company : companies) {
                items.add(new SearchResultItem(false, company));
            }
        }
        if (addresses != null && !addresses.isEmpty()) {
            items.add(new SearchResultItem(context.getString(R.string.search_locations)));
            for (Address address : addresses) {
                items.add(new SearchResultItem(false, address));
            }
        }
        if (items.isEmpty() && currentLocation != null) {
            items.add(new SearchResultItem(context.getString(R.string.near_me), currentLocation));
        }
        if (!recentSearches.isEmpty()) {
            items.add(new SearchResultItem(context.getString(R.string.search_recent)));
            items.addAll(recentSearches);
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
        View view;
        final SearchResultItem searchItem = getItem(position);

        if (searchItem.getType() == SearchResultItem.Type.HEADER) {
            final SearchHeaderView headerView;
            if (convertView != null && convertView instanceof SearchHeaderView) {
                headerView = (SearchHeaderView) convertView;
            } else {
                headerView = SearchHeaderView_.build(context);
            }
            headerView.setTitle(searchItem.getName());
            view = headerView;
        } else {
            final SearchItemView itemView;
            if (convertView != null && convertView instanceof SearchItemView) {
                itemView = (SearchItemView) convertView;
            } else {
                itemView = SearchItemView_.build(context);
            }

            itemView.setClickable(true);
            itemView.setOnClickListener(this);

            itemView.setSearchItem(searchItem);
            view = itemView;
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof SearchItemView) {
            SearchItemView view = (SearchItemView) v;
            if (listener != null) listener.onItemClick(view.getSearchItem(), view);
        }
    }

    public Context getContext() {
        return context;
    }

    public Listener getListener() {
        return listener;
    }

    public List<SearchResultItem> getResultItems() {
        return resultItems;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setRecentSearches(List<SearchResultItem> recentSearches) {
        this.recentSearches = recentSearches;
    }
}