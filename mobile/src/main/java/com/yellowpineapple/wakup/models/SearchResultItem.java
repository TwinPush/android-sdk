package com.yellowpineapple.wakup.models;

import android.location.Address;
import android.text.TextUtils;

import com.yellowpineapple.wakup.utils.Strings;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Created by agutierrez on 17/12/15.
 */
public class SearchResultItem {

    enum Type {
        COMPANY, LOCATION;
    }


    static final int MAX_FIELDS = 3;

    @Getter
    boolean recent = false;
    @Getter Type type;
    @Getter String name;
    @Getter
    Address address;
    @Getter Company company;

    public SearchResultItem(boolean recent, Address address) {
        this.recent = recent;
        this.type = Type.LOCATION;
        String[] fields = new String[] {
                address.getFeatureName(),
                address.getLocality(),
                address.getSubAdminArea(),
                address.getAdminArea()};
        List<String> info = new ArrayList<>();
        for (String field : fields) {
            if (Strings.notEmpty(field) &&
                    !info.contains(field) &&
                    // Limit displayed fields
                    info.size() < MAX_FIELDS) {
                info.add(field);
            }
        }
        this.name = TextUtils.join(", ", info);
        this.address = address;
        this.company = null;
    }

    public SearchResultItem(boolean recent, Company company) {
        this.recent = recent;
        this.type = Type.LOCATION;
        this.name = company.getName();
        this.address = null;
        this.company = company;
    }
}
