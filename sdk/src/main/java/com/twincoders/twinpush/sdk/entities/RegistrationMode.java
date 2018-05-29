package com.twincoders.twinpush.sdk.entities;

/**
 * Transport entity that is used to define the mode for registering in TwinPush platform
 */
public enum RegistrationMode {
    /**
     * The SDK will make the registration request to the TwinPush API, and manage the process in
     * stand-alone mode
     */
    INTERNAL(0),
    /**
     * The SDK will report the registration intent, and will wait for the external process to end
     */
    EXTERNAL(1);

    int id;

    RegistrationMode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static RegistrationMode fromId(int id) {
        for (RegistrationMode mode : values()) {
            if (mode.getId() == id) {
                return mode;
            }
        }
        // Default to internal
        return INTERNAL;
    }
}
