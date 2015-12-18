package com.yellowpineapple.wakup.models;

import java.io.Serializable;

import lombok.Getter;

/**
 * Created by agutierrez on 15/12/15.
 */
public class Company implements Serializable {

    @Getter int id;
    @Getter String name;

}
