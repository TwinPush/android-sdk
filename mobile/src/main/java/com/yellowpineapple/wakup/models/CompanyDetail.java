package com.yellowpineapple.wakup.models;

import java.io.Serializable;

/**
 * Created by agutierrez on 02/02/15.
 */
public class CompanyDetail implements Serializable {

    int id;
    String name;
    RemoteImage logo;
    int offerCount;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RemoteImage getLogo() {
        return logo;
    }

    public int getOfferCount() {
        return offerCount;
    }
}
