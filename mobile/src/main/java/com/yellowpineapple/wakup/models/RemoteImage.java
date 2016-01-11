package com.yellowpineapple.wakup.models;

import java.io.Serializable;

/**
 * Created by agutierrez on 02/02/15.
 */
public class RemoteImage implements Serializable {

    int height;
    int width;
    String rgbColor;
    String url;

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getRgbColor() {
        return rgbColor;
    }

    public String getUrl() {
        return url;
    }
}
