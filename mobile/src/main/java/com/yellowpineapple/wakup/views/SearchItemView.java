package com.yellowpineapple.wakup.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowpineapple.wakup.R;
import com.yellowpineapple.wakup.models.SearchResultItem;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import lombok.Getter;

@EViewGroup(R.layout.list_item_search)
public class SearchItemView extends FrameLayout {

    @Getter SearchResultItem searchItem;

    /* Views */
    @ViewById TextView txtName;
    @ViewById TextView txtDescription;
    @ViewById ImageView imgIcon;

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

    }

    public void setSearchItem(SearchResultItem searchItem) {
        this.searchItem = searchItem;
        if (searchItem != null) {
            txtName.setText(searchItem.getName());
            txtDescription.setText(searchItem.getDescription());
            txtDescription.setVisibility(VISIBLE);
            switch (searchItem.getType()) {
                case COMPANY:
                    imgIcon.setImageResource(R.drawable.ic_search_brand);
                    txtDescription.setVisibility(GONE);
                    break;
                case NEAR_ME:
                    txtDescription.setVisibility(GONE);
                case LOCATION:
                    imgIcon.setImageResource(R.drawable.ic_search_geo);
                    break;
            }
        } else {
            txtName.setText(null);
            txtDescription.setText(null);
            imgIcon.setImageDrawable(null);
        }
    }
}
