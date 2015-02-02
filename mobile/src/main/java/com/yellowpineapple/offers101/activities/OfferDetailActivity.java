package com.yellowpineapple.offers101.activities;

import android.support.v7.app.ActionBarActivity;

import com.yellowpineapple.offers101.R;
import com.yellowpineapple.offers101.models.Offer;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

@EActivity(R.layout.activity_offer_detail)
public class OfferDetailActivity extends ActionBarActivity {

    @Extra Offer offer;

    @Click(R.id.button)
    void onButtonPressed() {
        OfferDetailActivity_.intent(this).start();
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_back);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_right);
    }
}
