package com.yellowpineapple.wakup.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.models.Category;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EViewGroup(R.layout.view_search_filters)
public class SearchFiltersView extends FrameLayout {

    @ViewById OfferActionButton btnLeisure;
    @ViewById OfferActionButton btnRestaurants;
    @ViewById OfferActionButton btnServices;
    @ViewById OfferActionButton btnShopping;

    public SearchFiltersView(Context context) {
        super(context);
        init(null, 0);
    }

    public SearchFiltersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SearchFiltersView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

    }

    public List<Category> getSelectedCategories() {
        List<Category> categories = new ArrayList<>();
        if (btnLeisure.isSelected()) categories.add(Category.LEISURE);
        if (btnRestaurants.isSelected()) categories.add(Category.RESTAURANTS);
        if (btnServices.isSelected()) categories.add(Category.SERVICES);
        if (btnShopping.isSelected()) categories.add(Category.SHOPPING);
        return categories;
    }

    @Click
    void btnLeisure() {
        btnLeisure.setSelected(!btnLeisure.isSelected());
    }

    @Click
    void btnRestaurants() {
        btnRestaurants.setSelected(!btnRestaurants.isSelected());
    }

    @Click
    void btnServices() {
        btnServices.setSelected(!btnServices.isSelected());
    }

    @Click
    void btnShopping() {
        btnShopping.setSelected(!btnShopping.isSelected());
    }
}
