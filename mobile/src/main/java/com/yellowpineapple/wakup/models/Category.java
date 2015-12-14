package com.yellowpineapple.wakup.models;

import com.yellowpineapple.wakup.R;

import lombok.Getter;

/**
 * Created by agutierrez on 10/02/15.
 */
public enum Category {

    UNKNOWN(null, R.string.category_unknown, R.drawable.ic_pin_unknown),
    LEISURE("leisure", R.string.category_leisure, R.drawable.ic_pin_leisure),
    RESTAURANTS("restaurants", R.string.category_restaurants, R.drawable.ic_pin_restaurant),
    SERVICES("services", R.string.category_services, R.drawable.ic_pin_services),
    SHOPPING("shopping", R.string.category_shopping, R.drawable.ic_pin_shopping);

    @Getter String identifier;
    @Getter int nameResId;
    @Getter int iconResId;

    private Category(String identifier, int nameResId, int iconResId) {
        this.identifier = identifier;
        this.nameResId = nameResId;
        this.iconResId = iconResId;
    }

    public static Category fromIdentifier(String identifier) {
        Category value = UNKNOWN;
        for (Category category : Category.values()) {
            if (category != UNKNOWN) {
                if (category.getIdentifier().equals(identifier)) {
                    value = category;
                }
            }
        }
        return value;
    }
}
