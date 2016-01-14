package com.yellowpineapple.wakup.sdk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.yellowpineapple.wakup.sdk.R;

/**
 * Created by agutierrez on 05/02/15.
 */
public class NavBarButton extends FrameLayout {

    CharSequence text;
    Drawable icon;

    /* Views */
    ImageView imgIcon;
    TextView txtAction;

    /* Constructors */
    public NavBarButton(Context context) {
        this(context, null);
    }

    public NavBarButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavBarButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // Extract styleable attributes
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NavBarButton);
            if (a.hasValue(R.styleable.NavBarButton_navText)) {
                String text = a.getString(R.styleable.NavBarButton_navText);
                setText(text);
            }
            if (a.hasValue(R.styleable.NavBarButton_navIcon)) {
                Drawable icon = a.getDrawable(R.styleable.NavBarButton_navIcon);
                setIcon(icon);
            }
            a.recycle();
        }
        init();
    }

    private void init() {
        injectViews();
        // Set text
        setText(text);
        // Set image
        setIcon(icon);
    }

    private void injectViews() {
        inflate(getContext(), R.layout.wk_view_navbar_button, this);
        imgIcon = (ImageView) findViewById(R.id.imgIcon);
        txtAction = (TextView) findViewById(R.id.txtAction);
    }

    public void setText(int textResId) {
        setText(getResources().getText(textResId));
    }

    public void setText(CharSequence text) {
        this.text = text;
        if (txtAction != null) {
            txtAction.setText(text);
        }
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
        if (imgIcon != null) {
            imgIcon.setImageDrawable(icon);
        }
    }
}