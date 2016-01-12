package com.yellowpineapple.wakup.sdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.models.Category;

import java.util.ArrayList;
import java.util.List;

public class SearchFiltersView extends FrameLayout {

    OfferActionButton btnLeisure;
    OfferActionButton btnRestaurants;
    OfferActionButton btnServices;
    OfferActionButton btnShopping;

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
        injectViews();
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_search_filters, this);
        btnLeisure = ((OfferActionButton) findViewById(R.id.btnLeisure));
        btnServices = ((OfferActionButton) findViewById(R.id.btnServices));
        btnRestaurants = ((OfferActionButton) findViewById(R.id.btnRestaurants));
        btnShopping = ((OfferActionButton) findViewById(R.id.btnShopping));

        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
            }
        };

        OfferActionButton[] buttons = new OfferActionButton[] {
                btnRestaurants, btnLeisure, btnServices, btnShopping
        };
        for (OfferActionButton button : buttons) {
            button.setOnClickListener(onClickListener);
        }
    }

    public List<Category> getSelectedCategories() {
        List<Category> categories = new ArrayList<>();
        if (btnLeisure.isSelected()) categories.add(Category.LEISURE);
        if (btnRestaurants.isSelected()) categories.add(Category.RESTAURANTS);
        if (btnServices.isSelected()) categories.add(Category.SERVICES);
        if (btnShopping.isSelected()) categories.add(Category.SHOPPING);
        return categories;
    }
}
