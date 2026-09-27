package com.twincoders.twinpush.sdk.communications.pinning;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PinProtocol {
    static final int MAX_RESPONSE = 16 * 1024;
    static final String KEY_FORMAT = "twinpush-pinning-key-v1";
    static final String FORMAT = "twinpush-certificate-pins-v1";
    static final String PREFIX = "tp-pinning-v1:";

    static void validateAnchor(String anchor) {
        try {
            require(anchor != null && anchor.matches("tp-pinning-v1:[A-Za-z0-9_-]{43}"), "invalid integration key");
            byte[] bytes = PinEncoding.decode(anchor.substring(PREFIX.length()).replace('-', '+').replace('_', '/') + "=");
            require(bytes.length == 32, "invalid integration key");
        } catch (PinningException e) { throw new IllegalArgumentException(e.getMessage(), e); }
    }

    static final class Key {
        final String environment, id;
        final RSAPublicKey rsa;
        Key(String environment, String id, RSAPublicKey rsa) { this.environment = environment; this.id = id; this.rsa = rsa; }
    }
    static final class Pins {
        final Key key;
        final long version, expires;
        final List<String> pins;
        final byte[] canonical;
        Pins(Key key, long version, long expires, List<String> pins, byte[] canonical) {
            this.key = key; this.version = version; this.expires = expires;
            this.pins = Collections.unmodifiableList(new ArrayList<>(pins)); this.canonical = canonical;
        }
        void requireCurrent(long now) throws PinningException { require(expires > now, "pinset expired"); }
    }

    static Key key(byte[] json, String host, String anchor) throws PinningException {
        validateAnchor(anchor);
        Map<String, Object> object = StrictJson.object(json, MAX_RESPONSE);
        fields(object, "format", "environment", "algorithm", "key_id", "public_key");
        require(KEY_FORMAT.equals(string(object, "format")) && "RS256".equals(string(object, "algorithm")), "unsupported key protocol");
        String environment = string(object, "environment");
        require(environment.matches("[a-z0-9_-]+:[a-z0-9.-]+") && environment.substring(environment.indexOf(':') + 1).equals(host), "wrong environment host");
        Matcher pem = Pattern.compile("\\A-----BEGIN PUBLIC KEY-----\\r?\\n([A-Za-z0-9+/=\\r\\n]+)-----END PUBLIC KEY-----(?:\\r?\\n)?\\z")
                .matcher(string(object, "public_key"));
        require(pem.matches(), "expected a single SPKI PUBLIC KEY PEM");
        byte[] der = PinEncoding.decode(pem.group(1).replace("\r", "").replace("\n", ""));
        SpkiDer.validate(der);
        try {
            RSAPublicKey rsa = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
            require(rsa.getModulus().bitLength() >= 2048 && rsa.getModulus().bitLength() <= 8192, "RSA key size outside 2048..8192");
            // JCA may accept BER/trailing bytes; require the provider's canonical SPKI encoding.
            require(Arrays.equals(der, rsa.getEncoded()), "noncanonical SPKI DER");
            byte[] prefix = (KEY_FORMAT + "\n" + environment + "\n").getBytes(StandardCharsets.UTF_8);
            byte[] material = Arrays.copyOf(prefix, prefix.length + der.length);
            System.arraycopy(der, 0, material, prefix.length, der.length);
            String calculated = PREFIX + PinEncoding.url64(PinEncoding.hash(material));
            require(MessageDigest.isEqual(calculated.getBytes(StandardCharsets.US_ASCII), anchor.getBytes(StandardCharsets.US_ASCII)), "integration key mismatch");
            String id = string(object, "key_id");
            require(id.equals(PinEncoding.hex(PinEncoding.hash(der))), "key_id mismatch");
            return new Key(environment, id, rsa);
        } catch (PinningException e) { throw e; }
        catch (Exception e) { throw new PinningException("invalid RSA public key", e); }
    }

    static Pins pins(byte[] json, Key key) throws PinningException {
        Map<String, Object> object = StrictJson.object(json, MAX_RESPONSE);
        fields(object, "format", "algorithm", "environment", "key_id", "version", "valid_until", "pins", "signature");
        require(FORMAT.equals(string(object, "format")) && "RS256".equals(string(object, "algorithm")), "unsupported pins protocol");
        require(key.environment.equals(string(object, "environment")) && key.id.equals(string(object, "key_id")), "pins identity mismatch");
        Object number = object.get("version");
        require(number instanceof Long, "version must be an integer");
        long version = (Long) number;
        require(version >= 1 && version <= 9007199254740991L, "version out of range");
        String date = string(object, "valid_until");
        require(date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z"), "invalid UTC date");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        java.util.GregorianCalendar calendar = new java.util.GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        calendar.setGregorianChange(new java.util.Date(Long.MIN_VALUE));
        format.setCalendar(calendar); format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        java.util.Date expires = format.parse(date, position);
        require(expires != null && position.getIndex() == date.length() && format.format(expires).equals(date), "invalid UTC date");
        require(object.get("pins") instanceof List, "pins must be an array");
        List<?> input = (List<?>) object.get("pins");
        require(input.size() >= 1 && input.size() <= 32, "pin count out of range");
        List<String> pins = new ArrayList<>();
        String previous = null;
        for (Object item : input) {
            require(item instanceof String, "pin must be a string");
            String pin = (String) item;
            require(pin.startsWith("sha256/") && PinEncoding.decode(pin.substring(7)).length == 32, "invalid TLS pin");
            require(previous == null || previous.compareTo(pin) < 0, "pins must be distinct and ASCII sorted");
            pins.add(pin); previous = pin;
        }
        StringBuilder canonical = new StringBuilder(FORMAT + "\nRS256\n" + key.environment + "\n" + key.id + "\n" + version + "\n" + date + "\n" + pins.size() + "\n");
        for (String pin : pins) canonical.append(pin).append('\n');
        byte[] bytes = canonical.toString().getBytes(StandardCharsets.UTF_8);
        byte[] signature = PinEncoding.decode(string(object, "signature"));
        require(signature.length == (key.rsa.getModulus().bitLength() + 7) / 8, "invalid signature length");
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(key.rsa); verifier.update(bytes);
            require(verifier.verify(signature), "invalid pins signature");
        } catch (PinningException e) { throw e; }
        catch (Exception e) { throw new PinningException("signature verification failed", e); }
        return new Pins(key, version, expires.getTime(), pins, bytes);
    }

    static byte[] read(InputStream stream, int limit) throws IOException {
        return read(stream, limit, 0);
    }
    static byte[] read(InputStream stream, int limit, long deadline) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int n;
        while ((n = stream.read(buffer, 0, Math.min(buffer.length, limit + 1 - out.size()))) != -1) {
            require(deadline == 0 || System.nanoTime() - deadline < 0, "bootstrap deadline exceeded");
            out.write(buffer, 0, n);
            require(out.size() <= limit, "response exceeds size limit");
        }
        return out.toByteArray();
    }
    static void require(boolean ok, String message) throws PinningException { if (!ok) throw new PinningException(message); }
    static String string(Map<String, Object> object, String field) throws PinningException {
        require(object.get(field) instanceof String, field + " must be a string"); return (String) object.get(field);
    }
    private static void fields(Map<String, Object> object, String... fields) throws PinningException {
        require(object.keySet().equals(new HashSet<>(Arrays.asList(fields))), "missing or unknown fields");
    }
}
