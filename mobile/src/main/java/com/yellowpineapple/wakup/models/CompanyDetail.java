package com.yellowpineapple.wakup.models;

import java.io.Serializable;

import lombok.Getter;

/**
 * Created by agutierrez on 02/02/15.
 */
public class CompanyDetail implements Serializable {

    @Getter int id;
    @Getter String name;
    @Getter RemoteImage logo;
    @Getter int offerCount;

}
