package com.yellowpineapple.offers101.models;

import com.yellowpineapple.offers101.R;

import lombok.Getter;

/**
 * Created by agutierrez on 10/02/15.
 */
public enum Category {

    UNKNOWN(null, R.string.category_unknown, 0),
    LEISURE("leisure", R.string.category_leisure, 0),
    RESTAURANTS("restaurants", R.string.category_leisure, 0),
    SERVICES("services", R.string.category_leisure, 0),
    SHOPPING("shopping", R.string.category_leisure, 0);

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
