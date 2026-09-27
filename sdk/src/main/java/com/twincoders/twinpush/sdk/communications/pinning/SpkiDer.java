package com.twincoders.twinpush.sdk.communications.pinning;

import java.util.Arrays;

/** Strict DER checks independent of a provider's willingness to accept BER encodings. */
final class SpkiDer {
    private final byte[] data;
    private int position;
    private SpkiDer(byte[] data) { this.data = data; }
    static void validate(byte[] data) throws PinningException {
        SpkiDer outer = new SpkiDer(data);
        SpkiDer spki = new SpkiDer(outer.value(0x30)); outer.end();
        // rsaEncryption AlgorithmIdentifier, with the required NULL parameters.
        byte[] algorithm = {6, 9, 42, (byte) 134, 72, (byte) 134, (byte) 247, 13, 1, 1, 1, 5, 0};
        PinProtocol.require(Arrays.equals(algorithm, spki.value(0x30)), "expected RSA SPKI AlgorithmIdentifier");
        byte[] bits = spki.value(3); spki.end();
        PinProtocol.require(bits.length > 1 && bits[0] == 0, "invalid SPKI bit string");
        SpkiDer inner = new SpkiDer(Arrays.copyOfRange(bits, 1, bits.length));
        SpkiDer rsa = new SpkiDer(inner.value(0x30)); inner.end();
        integer(rsa.value(2)); integer(rsa.value(2)); rsa.end();
    }
    private static void integer(byte[] bytes) throws PinningException {
        PinProtocol.require(bytes.length > 0 && (bytes[0] & 128) == 0, "invalid RSA integer");
        PinProtocol.require(bytes.length == 1 || bytes[0] != 0 || (bytes[1] & 128) != 0, "noncanonical RSA integer");
    }
    private int next() throws PinningException {
        PinProtocol.require(position < data.length, "truncated SPKI DER"); return data[position++] & 255;
    }
    private byte[] value(int tag) throws PinningException {
        PinProtocol.require(next() == tag, "invalid SPKI DER tag");
        int size = next();
        if (size >= 128) {
            int count = size & 127;
            PinProtocol.require(count >= 1 && count <= 3, "invalid SPKI DER length");
            size = next(); PinProtocol.require(size != 0, "noncanonical SPKI DER length");
            for (int i = 1; i < count; i++) size = (size << 8) | next();
            PinProtocol.require(size >= 128, "noncanonical SPKI DER length");
        }
        PinProtocol.require(size <= data.length - position, "truncated SPKI DER value");
        byte[] result = Arrays.copyOfRange(data, position, position + size); position += size; return result;
    }
    private void end() throws PinningException { PinProtocol.require(position == data.length, "trailing SPKI DER data"); }
}
