package com.twincoders.twinpush.sdk.communications.pinning;

import java.io.IOException;

/** A fail-closed remote pinning error, delivered through the normal request error callback. */
public final class PinningException extends IOException {
    public PinningException(String message) { super("TwinPush certificate pinning: " + message); }
    public PinningException(String message, Throwable cause) { super("TwinPush certificate pinning: " + message, cause); }
}
