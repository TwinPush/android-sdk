package com.yellowpineapple.offers101.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.yellowpineapple.offers101.models.RemoteImage;

/**
 * Created by agutierrez on 04/02/15.
 */
public class RemoteImageView extends AspectKeepFrameLayout {

    RemoteImage image;
    ImageView imageView;

    public RemoteImageView(Context context) {
        super(context);
    }

    public RemoteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    void init(AttributeSet attrs, int defStyle) {
        imageView = new ImageView(getContext());
        addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setImage(RemoteImage image) {
        this.image = image;
        if (image != null) {
            this.setVirtualSize(image.getHeight(), image.getWidth());
            int color = Color.parseColor(String.format("#%s",image.getRgbColor()));
            ColorDrawable drawable = new ColorDrawable(color);
            setBackgroundColor(color);
            Ion.with(imageView)
                    .placeholder(drawable)
                    .error(drawable)
                    .animateIn(android.R.anim.fade_in)
                    .fitXY()
                    .load(image.getUrl());
        }
    }
}
