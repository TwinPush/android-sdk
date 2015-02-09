package com.yellowpineapple.offers101.activities;

import android.app.ActionBar;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;
import com.yellowpineapple.offers101.utils.Strings;
import com.yellowpineapple.offers101.views.RemoteImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_offer_detail)
public class OfferDetailActivity extends ParentActivity {

    @Extra Offer offer;
    @Extra Location location;

    int scrollPosition = 0;
    int scrollUpStart = 0;
    int scrollDownStart = 0;

    private ActionBar mActionBar;
    private float mActionBarHeight;

    /* Views */
    @ViewById ScrollView scrollView;
    @ViewById RemoteImageView imgCompany;
    @ViewById TextView txtCompany;
    @ViewById TextView txtAddress;
    @ViewById TextView txtDistance;
    @ViewById RemoteImageView imgOffer;
    @ViewById TextView txtShortDescription;
    @ViewById TextView txtDescription;
    @ViewById TextView txtShortOffer;
    @ViewById TextView txtExpiration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void afterViews() {
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        mActionBarHeight = styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        mActionBar = getActionBar();
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

        if (offer != null) {
            imgCompany.setImage(offer.getCompany().getLogo());
            imgOffer.setImage(offer.getImage(), offer.getThumbnail());
            txtCompany.setText(offer.getCompany().getName());
            txtDistance.setText(offer.getHumanizedDistance(getApplicationContext(), location));
            if (offer.getStore() != null) {
                txtAddress.setText(offer.getStore().getAddress());
                txtAddress.setVisibility(View.VISIBLE);
            } else {
                txtAddress.setVisibility(View.GONE);
            }
            txtDescription.setVisibility(Strings.isEmpty(offer.getDescription()) ? View.GONE : View.VISIBLE);
            txtDescription.setText(offer.getDescription());
            txtShortDescription.setText(offer.getShortDescription());
            txtShortOffer.setText(offer.getShortOffer());
            txtExpiration.setText(offer.getHumanizedExpiration(this));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right);
    }
}
