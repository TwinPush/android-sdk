package com.yellowpineapple.wakup.views;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.models.Offer;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import lombok.Getter;
import me.grantland.widget.AutofitTextView;

@EViewGroup(resName="list_item_offer")
public class OfferListView extends FrameLayout {

    @Getter Offer offer;

    /* Views */
    @ViewById RemoteImageView offerImageView;
    @ViewById TextView txtCompany;
    @ViewById TextView txtDescription;
    @ViewById RelativeLayout viewShortOffer;
    @ViewById TextView txtDistance;
    @ViewById TextView txtExpiration;

    float shortOfferSize = 0;

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

    public void setOffer(Offer offer, Location currentLocation) {
        this.offer = offer;
        offerImageView.setImage(offer.getThumbnail());
        txtCompany.setText(offer.getCompany().getName());
        txtDescription.setText(offer.getShortDescription());
        txtDistance.setText(offer.getHumanizedDistance(getContext(), currentLocation));
        txtExpiration.setText(offer.getHumanizedExpiration(getContext()));
        createShortOfferLabel(offer);
    }

    private void createShortOfferLabel(Offer offer) {
        viewShortOffer.removeAllViews();

        float maxSize = getContext().getResources().getDimension(R.dimen.title_text);
        float minSize = getContext().getResources().getDimension(R.dimen.small_text);

        AutofitTextView txtShortOffer = (AutofitTextView) ((Activity) getContext()).getLayoutInflater().inflate(R.layout.textview_shortoffer, null);
        txtShortOffer.setMaxTextSize(TypedValue.COMPLEX_UNIT_PX, maxSize);
        txtShortOffer.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, minSize);
        txtShortOffer.setText(offer.getShortOffer());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        viewShortOffer.addView(txtShortOffer, layoutParams);
    }


}
