package com.yellowpineapple.wakup.sdk.activities;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.communications.Request;
import com.yellowpineapple.wakup.sdk.communications.requests.search.SearchRequest;
import com.yellowpineapple.wakup.sdk.controllers.SearchResultAdapter;
import com.yellowpineapple.wakup.sdk.models.SearchResult;
import com.yellowpineapple.wakup.sdk.models.SearchResultItem;
import com.yellowpineapple.wakup.sdk.utils.IntentBuilder;
import com.yellowpineapple.wakup.sdk.utils.Ln;
import com.yellowpineapple.wakup.sdk.utils.Strings;
import com.yellowpineapple.wakup.sdk.views.SearchFiltersView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends ParentActivity {

    final static private int REQUEST_DELAY = 200;
    final static int MAX_GEO_RESULTS = 5;
    public final static String LOCATION_EXTRA = "location";
    final private Handler searchHandler = new Handler();
    final private Handler geoSearchHandler = new Handler();
    final private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    SearchView searchView;
    Request searchRequest = null;
    Location location = null;

    // Views
    ListView listView;
    SearchResultAdapter listAdapter;
    SearchFiltersView filtersView;

    String searchQuery = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wk_activity_search);
        injectExtras();
        injectViews();
    }

    private void injectViews() {
        listView = ((ListView) findViewById(R.id.list_view));
        afterViews();
    }

    private void injectExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras!= null) {
            if (extras.containsKey(LOCATION_EXTRA)) {
                location = extras.getParcelable(LOCATION_EXTRA);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.wk_search_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.setQuery("", false);
                return true;
            }
        });
        // Hide Keyboard when keyboard Action button is pressed
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView editText = (TextView) searchView.findViewById(id);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    void afterViews() {
        listAdapter = new SearchResultAdapter(this, location, getPersistence().getRecentSearches());
        listAdapter.setListener(new SearchResultAdapter.Listener() {
            @Override
            public void onItemClick(SearchResultItem item, View view) {
                SearchResultActivity.intent(SearchActivity.this).
                        searchItem(item).
                        categories(filtersView.getSelectedCategories()).
                        start();
                slideInTransition();
                if (item.getType() == SearchResultItem.Type.COMPANY ||
                        item.getType() == SearchResultItem.Type.LOCATION) {
                    getPersistence().addRecentSearch(item);
                    listAdapter.setRecentSearches(getPersistence().getRecentSearches());
                    refreshList();
                }
            }
        });
        filtersView = new SearchFiltersView(this);
        listView.addHeaderView(filtersView, null, false);
        listView.setAdapter(listAdapter);
        refreshList();
    }

    void search(final String query) {
        // Cancel previous timer and request
        searchHandler.removeCallbacks(searchRunnable);
        if (searchRequest != null) {
            searchRequest.cancel();
        }
        this.searchQuery = query;
        // Start countdown to perform search
        searchHandler.postDelayed(searchRunnable, REQUEST_DELAY);
    }

    void refreshList() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    void geoSearch(final String query, final String country, final String countryCode) {
        geoSearchHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        if (Geocoder.isPresent()) {

                            Geocoder geocoder = new Geocoder(SearchActivity.this);
                            try {
                                List<Address> addresses = geocoder.getFromLocationName(String.format("%s, %s", query, country), MAX_GEO_RESULTS);
                                List<Address> validAddresses = new ArrayList<>();
                                for (Address address : addresses) {
                                    // Only include addresses from selected country
                                    if (Strings.equals(countryCode, address.getCountryCode())) {
                                        validAddresses.add(address);
                                    }
                                }
                                // Check if query is still valid
                                if (Strings.equals(query, searchQuery)) {
                                    listAdapter.setAddresses(validAddresses);
                                    refreshList();
                                }
                            } catch (Exception exception) {
                                Ln.e(exception);
                                listAdapter.setAddresses(new ArrayList<Address>());
                                refreshList();
                            }
                        } else {
                            Ln.e("Can not perform search: Geocoder is not present in this device");
                        }
                    }
                });
    }

    Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            final String query = searchQuery;
            if (Strings.notEmpty(query)) {
                searchRequest = getRequestClient().search(query.trim(), new SearchRequest.Listener() {
                    @Override
                    public void onSuccess(SearchResult searchResult) {
                        searchRequest = null;
                        // Check if query is still valid
                        if (Strings.equals(query, searchQuery)) {
                            listAdapter.setCompanies(searchResult.getCompanies());
                            refreshList();
                        }
                    }

                    @Override
                    public void onError(Exception exception) {
                        searchRequest = null;
                        Ln.e(exception);
                        Toast.makeText(SearchActivity.this, R.string.wk_search_error, Toast.LENGTH_LONG).show();
                    }
                });
                // TODO Allow country setup for SDK
                geoSearch(query.trim(), "Spain", "ES");
            } else {
                listAdapter.setCompanies(null);
                listAdapter.setAddresses(null);
                refreshList();
            }
        }
    };

    // Builder

    public static Builder intent(Context context) {
        return new Builder(context);
    }

    public static class Builder extends IntentBuilder<SearchActivity> {

        public Builder(Context context) {
            super(SearchActivity.class, context);
        }

        public Builder location(Location location) {
            getIntent().putExtra(LOCATION_EXTRA, location);
            return this;
        }
    }
}
