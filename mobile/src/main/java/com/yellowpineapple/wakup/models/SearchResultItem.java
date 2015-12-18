package com.yellowpineapple.wakup.models;

import android.location.Address;
import android.location.Location;
import android.text.TextUtils;

import com.yellowpineapple.wakup.utils.Strings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by agutierrez on 17/12/15.
 */
public class SearchResultItem implements Serializable {

    public enum Type {
        COMPANY, LOCATION;
    }

    static final int MAX_FIELDS = 3;

    @Setter @Getter boolean recent = false;
    @Getter Type type;
    @Getter String name;
    @Getter String description;
    @Getter double latitude;
    @Getter double longitude;
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
        this.description = TextUtils.join(", ", info);
        this.name = address.getFeatureName();
        this.latitude = address.getLatitude();
        this.longitude = address.getLongitude();
        this.company = null;
    }

    public Location getLocation() {
        Location mLocation = new Location(this.name);
        mLocation.setLatitude(getLatitude());
        mLocation.setLongitude(getLongitude());
        return mLocation;
    }

    public SearchResultItem(boolean recent, Company company) {
        this.recent = recent;
        this.type = Type.COMPANY;
        this.name = this.description = company.getName();
        this.company = company;
    }
}
