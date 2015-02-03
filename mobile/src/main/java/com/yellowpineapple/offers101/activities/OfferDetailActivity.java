package com.yellowpineapple.offers101.activities;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ScrollView;

import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_offer_detail)
public class OfferDetailActivity extends ParentActivity {

    @Extra Offer offer;
    boolean actionBarVisible = true;

    int scrollPosition = 0;
    int scrollUpStart = 0;
    int scrollDownStart = 0;

    private ActionBar mActionBar;
    private float mActionBarHeight;

    /* Views */
    @ViewById
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void afterViews() {
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        mActionBarHeight = styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        mActionBar = getSupportActionBar();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(
           new ViewTreeObserver.OnScrollChangedListener() {
               @Override
               public void onScrollChanged() {
                   int scrollY = scrollView.getScrollY();
                   if (scrollY > scrollPosition) {
                       // Scrolling Down
                       if (scrollY - scrollDownStart > mActionBarHeight) {
                           mActionBar.hide();
                       }
                       scrollUpStart = scrollY;
                   } else {
                       // Scrolling Up
                       if (scrollY < mActionBarHeight || scrollUpStart - scrollY > (mActionBarHeight / 2)) {
                           mActionBar.show();
                       }
                       scrollDownStart = scrollY;
                   }
                   scrollPosition = scrollY;
               }
           }
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right);
    }

    @Click(R.id.button)
    void onButtonPressed() {
        OfferDetailActivity_.intent(this).start();
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back);
    }

    @Click
    void toggleNavButton() {
        ActionBar ab = getSupportActionBar();
//        if (scrollState == ScrollState.UP) {
//            if (ab.isShowing()) {
//                ab.hide();
//            }
//        } else if (scrollState == ScrollState.DOWN) {
//            if (!ab.isShowing()) {
//                ab.show();
//            }
//        }
        if (actionBarVisible) {
            ab.hide();
        } else {
            ab.show();
        }
        actionBarVisible = !actionBarVisible;

    }
}
