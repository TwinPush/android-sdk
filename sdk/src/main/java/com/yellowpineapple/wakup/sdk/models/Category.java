package com.yellowpineapple.wakup.sdk.models;

import com.yellowpineapple.wakup.sdk.R;

/**
 * Created by agutierrez on 10/02/15.
 */
public enum Category {

    UNKNOWN(null, R.string.wk_category_unknown, R.drawable.wk_pin_unknown),
    LEISURE("leisure", R.string.wk_category_leisure, R.drawable.wk_pin_leisure),
    RESTAURANTS("restaurants", R.string.wk_category_restaurants, R.drawable.wk_pin_restaurants),
    SERVICES("services", R.string.wk_category_services, R.drawable.wk_pin_services),
    SHOPPING("shopping", R.string.wk_category_shopping, R.drawable.wk_pin_shopping);

    String identifier;
    int nameResId;
    int iconResId;

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

    public String getIdentifier() {
        return identifier;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getNameResId() {
        return nameResId;
    }
}
