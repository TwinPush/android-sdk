package com.yellowpineapple.offers101.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.yellowpineapple.offers101.R;

import org.androidannotations.annotations.EViewGroup;

/**
 * Created by agutierrez on 20/02/15.
 */
@EViewGroup(R.layout.view_related_offers_header)
public class RelatedOffersHeader extends FrameLayout {

    public RelatedOffersHeader(Context context) {
        super(context);
    }

    public RelatedOffersHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelatedOffersHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
