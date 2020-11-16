package com.twincoders.twinpush.sdk.entities;

public enum Platform {
    ANDROID("android"),
    HUAWEI("huawei");

    private final String key;
    Platform(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}