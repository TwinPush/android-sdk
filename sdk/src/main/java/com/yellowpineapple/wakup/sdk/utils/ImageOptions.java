package com.yellowpineapple.wakup.sdk.utils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * Created by agutierrez on 15/1/16.
 */
public class ImageOptions {

    public static DisplayImageOptions get() {
        return builder().build();
    }

    public static DisplayImageOptions.Builder builder() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true);
    }

}
