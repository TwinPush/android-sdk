package com.yellowpineapple.wakup.models;

import java.io.Serializable;

import lombok.Getter;

/**
 * Created by agutierrez on 02/02/15.
 */
public class RemoteImage implements Serializable {

    @Getter int height;
    @Getter int width;
    @Getter String rgbColor;
    @Getter String url;

}
