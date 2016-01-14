package com.yellowpineapple.wakup.sdk.views;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.models.Offer;

import me.grantland.widget.AutofitTextView;

public class OfferListView extends FrameLayout {

    Offer offer;

    /* Views */
    RemoteImageView offerImageView;
    TextView txtCompany;
    TextView txtDescription;
    RelativeLayout viewShortOffer;
    TextView txtDistance;
    TextView txtExpiration;

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
        injectViews();
    }

    private void injectViews() {
        inflate(getContext(), R.layout.wk_list_item_offer, this);
        txtDistance = ((TextView) findViewById(R.id.txtDistance));
        viewShortOffer = ((RelativeLayout) findViewById(R.id.viewShortOffer));
        txtExpiration = ((TextView) findViewById(R.id.txtExpiration));
        txtDescription = ((TextView) findViewById(R.id.txtDescription));
        txtCompany = ((TextView) findViewById(R.id.txtCompany));
        offerImageView = ((RemoteImageView) findViewById(R.id.offerImageView));
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

        float maxSize = getContext().getResources().getDimension(R.dimen.wk_title_text);
        float minSize = getContext().getResources().getDimension(R.dimen.wk_small_text);

        AutofitTextView txtShortOffer = (AutofitTextView) ((Activity) getContext()).getLayoutInflater().inflate(R.layout.wk_textview_shortoffer, null);
        txtShortOffer.setMaxTextSize(TypedValue.COMPLEX_UNIT_PX, maxSize);
        txtShortOffer.setMinTextSize(TypedValue.COMPLEX_UNIT_PX, minSize);
        txtShortOffer.setText(offer.getShortOffer());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        viewShortOffer.addView(txtShortOffer, layoutParams);
    }

    public Offer getOffer() {
        return offer;
    }

}
