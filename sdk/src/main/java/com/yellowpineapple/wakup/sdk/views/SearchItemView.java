package com.yellowpineapple.wakup.sdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowpineapple.wakup.sdk.R;
import com.yellowpineapple.wakup.sdk.models.SearchResultItem;

public class SearchItemView extends FrameLayout {

    SearchResultItem searchItem;

    /* Views */
    TextView txtName;
    TextView txtDescription;
    ImageView imgIcon;

    public SearchItemView(Context context) {
        super(context);
        init(null, 0);
    }

    public SearchItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SearchItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        injectViews();
    }

    private void injectViews() {
        inflate(getContext(), R.layout.wk_list_item_search, this);
        imgIcon = ((ImageView) findViewById(R.id.imgIcon));
        txtDescription = ((TextView) findViewById(R.id.txtDescription));
        txtName = ((TextView) findViewById(R.id.txtName));
    }

    public void setSearchItem(SearchResultItem searchItem) {
        this.searchItem = searchItem;
        if (searchItem != null) {
            txtName.setText(searchItem.getName());
            txtDescription.setText(searchItem.getDescription());
            txtDescription.setVisibility(VISIBLE);
            switch (searchItem.getType()) {
                case COMPANY:
                    imgIcon.setImageResource(R.drawable.wk_ic_search_brand);
                    txtDescription.setVisibility(GONE);
                    break;
                case NEAR_ME:
                    txtDescription.setVisibility(GONE);
                case LOCATION:
                    imgIcon.setImageResource(R.drawable.wk_ic_search_geo);
                    break;
            }
        } else {
            txtName.setText(null);
            txtDescription.setText(null);
            imgIcon.setImageDrawable(null);
        }
    }

    public SearchResultItem getSearchItem() {
        return searchItem;
    }
}
