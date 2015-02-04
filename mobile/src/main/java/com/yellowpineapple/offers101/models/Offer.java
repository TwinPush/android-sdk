package com.yellowpineapple.offers101.models;

import java.util.Date;

import lombok.Getter;

/**
 * Created by agutierrez on 02/02/15.
 */
public class Offer {

    @Getter int id;
    @Getter boolean online;
    @Getter Company company;
    @Getter String category;
    @Getter String description;
    @Getter String shortDescription;
    @Getter String shortOffer;
    @Getter Date expirationDate;
    @Getter RemoteImage image;
    @Getter RemoteImage thumbnail;
    @Getter Store store;

}
