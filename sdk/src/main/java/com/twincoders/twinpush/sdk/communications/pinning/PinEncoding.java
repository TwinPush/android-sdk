package com.twincoders.twinpush.sdk.communications.pinning;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Canonical RFC 4648 encoding, independent of Android APIs for JVM interoperability tests. */
final class PinEncoding {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    static String base64(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < bytes.length; i += 3) {
            int n = (bytes[i] & 255) << 16;
            if (i + 1 < bytes.length) n |= (bytes[i + 1] & 255) << 8;
            if (i + 2 < bytes.length) n |= bytes[i + 2] & 255;
            out.append(ALPHABET.charAt(n >>> 18)).append(ALPHABET.charAt((n >>> 12) & 63));
            out.append(i + 1 < bytes.length ? ALPHABET.charAt((n >>> 6) & 63) : '=');
            out.append(i + 2 < bytes.length ? ALPHABET.charAt(n & 63) : '=');
        }
        return out.toString();
    }
    static byte[] decode(String text) throws PinningException {
        if (text.length() == 0 || text.length() % 4 != 0) throw new PinningException("invalid Base64");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bits = 0, n = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '=') break;
            int v = ALPHABET.indexOf(c);
            if (v < 0) throw new PinningException("invalid Base64");
            n = (n << 6) | v; bits += 6;
            if (bits >= 8) { bits -= 8; out.write((n >>> bits) & 255); }
        }
        byte[] decoded = out.toByteArray();
        if (!base64(decoded).equals(text)) throw new PinningException("noncanonical Base64");
        return decoded;
    }
    static String url64(byte[] bytes) { return base64(bytes).replace('+', '-').replace('/', '_').replace("=", ""); }
    static byte[] hash(byte[] bytes) {
        try { return MessageDigest.getInstance("SHA-256").digest(bytes); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append("0123456789abcdef".charAt((b & 255) >>> 4)).append("0123456789abcdef".charAt(b & 15));
        return out.toString();
    }
}
