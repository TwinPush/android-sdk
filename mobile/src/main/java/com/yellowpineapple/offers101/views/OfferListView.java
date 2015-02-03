package com.yellowpineapple.offers101.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.yellowpineapple.offers101.R;

import org.androidannotations.annotations.EViewGroup;

@EViewGroup(R.layout.view_offer_list)
public class OfferListView extends FrameLayout {

    public OfferListView(Context context) {
        super(context);
        init(null, 0);
    }

    public OfferListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OfferListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

    }
}
