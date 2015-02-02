package com.yellowpineapple.offers101.models;

import java.util.Date;

import lombok.Getter;

/**
 * Created by agutierrez on 02/02/15.
 */
public class Offer {

    @Getter Company company;
    @Getter String category;
    @Getter String description;
    @Getter Date expirationDate;
    @Getter int id;

}
