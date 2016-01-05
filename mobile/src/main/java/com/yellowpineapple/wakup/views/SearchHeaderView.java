package com.yellowpineapple.wakup.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yellowpineapple.wakup.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.list_item_search_header)
public class SearchHeaderView extends FrameLayout {

    /* Views */
    @ViewById TextView txtName;

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

    }

    public void setTitle(String title) {
        txtName.setText(title);
    }
}
