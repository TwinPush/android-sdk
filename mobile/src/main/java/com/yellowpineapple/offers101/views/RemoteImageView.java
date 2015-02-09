package com.yellowpineapple.offers101.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;
import com.koushikdutta.ion.bitmap.PostProcess;
import com.yellowpineapple.offers101.models.RemoteImage;

import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.UiThread;

/**
 * Created by agutierrez on 04/02/15.
 */
@EView
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
        if (!isInEditMode()) {
            addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    public void setImage(RemoteImage image) {
        setImage(image, null);
    }

    public void setImage(final RemoteImage image, final RemoteImage thumbnail) {
        this.image = image;
        if (!isInEditMode()) {
            if (image != null) {
                this.setVirtualSize(image.getHeight(), image.getWidth());
                int color = Color.parseColor(String.format("#%s", image.getRgbColor()));
                Drawable placeholder = new ColorDrawable(color);
                setBackgroundColor(color);
                imageView.setImageDrawable(placeholder);
                if (thumbnail != null) {
                    loadImage(thumbnail, placeholder, new PostProcess() {
                        @Override
                        public void postProcess(BitmapInfo info) throws Exception {
                            Drawable thumbnailBitmap = new BitmapDrawable(getResources(), info.bitmap);
                            loadImage(image, thumbnailBitmap, null);
                        }

                        @Override
                        public String key() {
                            return thumbnail.getUrl();
                        }
                    });
                } else {
                    loadImage(image, placeholder, null);
                }
            }
        }
    }

    @UiThread
    void loadImage(RemoteImage image, Drawable placeholder, PostProcess postProcess) {

        if (postProcess != null) {
            Ion.with(imageView)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .fitXY()
                    .animateIn(android.R.anim.fade_in)
                    .postProcess(postProcess).load(image.getUrl());
        } else {
            Ion.with(imageView)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .fitXY()
                    .animateIn(android.R.anim.fade_in)
                    .load(image.getUrl());
        }
    }
}
