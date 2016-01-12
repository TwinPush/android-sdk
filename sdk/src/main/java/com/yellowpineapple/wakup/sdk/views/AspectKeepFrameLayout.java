package com.yellowpineapple.wakup.sdk.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.yellowpineapple.wakup.sdk.R;

public class AspectKeepFrameLayout extends FrameLayout {

    int virtualHeight;
    int virtualWidth;

    public AspectKeepFrameLayout(Context context) {
        super(context);
    }

    public AspectKeepFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AspectKeepFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    void init(AttributeSet attrs, int defStyle) {
        // Extract custom attributes
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AspectKeepFrameLayout, defStyle, 0);
        if (a != null) {
            virtualHeight = a.getInt(R.styleable.AspectKeepFrameLayout_virtualHeight, 1);
            virtualWidth = a.getInt(R.styleable.AspectKeepFrameLayout_virtualWidth, 1);
            a.recycle();
        }
    }

    // Overridden to retain aspect of this layout view
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!isInEditMode()) {
            int currentWidth = MeasureSpec.getSize(widthMeasureSpec);
            int currentHeight = MeasureSpec.getSize(heightMeasureSpec);
            double aspect = currentWidth / (double) currentHeight;
            // Those are from XML layout
            double virtualAspect = virtualWidth / (double) virtualHeight;
            int width, height;

            if (currentHeight != 0 && aspect > virtualAspect) {
                height = currentHeight;
                width = (int) Math.round(height * virtualAspect);
            } else {
                width = currentWidth;
                height = (int) Math.round(width / virtualAspect);
            }

            int newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

            measureChildren(newWidthMeasureSpec, newHeightMeasureSpec);

            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setVirtualSize(int height, int width) {
        virtualHeight = height;
        virtualWidth = width;
        refreshDrawableState();
    }
}