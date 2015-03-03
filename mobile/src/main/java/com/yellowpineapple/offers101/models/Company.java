package com.yellowpineapple.offers101.models;

import java.io.Serializable;

import lombok.Getter;

/**
 * Created by agutierrez on 02/02/15.
 */
public class Company implements Serializable {

    @Getter int id;
    @Getter RemoteImage logo;
    @Getter String name;
    @Getter int offerCount;

}
