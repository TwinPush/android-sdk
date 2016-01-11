package com.yellowpineapple.wakup.models;

import java.io.Serializable;

/**
 * Created by agutierrez on 15/12/15.
 */
public class Company implements Serializable {

    int id;
    String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
