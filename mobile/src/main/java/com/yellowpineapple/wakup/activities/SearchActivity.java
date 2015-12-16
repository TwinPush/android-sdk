package com.yellowpineapple.wakup.activities;

import android.view.Menu;

import com.yellowpineapple.wakup.R;

import org.androidannotations.annotations.EActivity;

/**
 * Created by agutierrez on 15/12/15.
 */

@EActivity(R.layout.activity_search)
public class SearchActivity extends ParentActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

}
