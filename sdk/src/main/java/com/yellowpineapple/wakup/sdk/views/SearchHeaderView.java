package com.yellowpineapple.wakup.sdk.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yellowpineapple.wakup.sdk.R;

public class SearchHeaderView extends FrameLayout {

    /* Views */
    TextView txtName;

    public SearchHeaderView(Context context) {
        super(context);
        init(null, 0);
    }

    public SearchHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SearchHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        injectViews();
    }

    private void injectViews() {
        inflate(getContext(), R.layout.wk_list_item_search_header, this);
        txtName = (TextView) findViewById(R.id.txtName);
    }

    public void setTitle(String title) {
        txtName.setText(title);
    }
}
