package com.twincoders.twinpush.sdk.communications.pinning;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** One atomic ledger per hostname, with high-water marks keyed by the full environment, not key ID. */
final class PinRepository {
    static final int MAX_STORE = 1024 * 1024;
    interface Transaction { byte[] apply(byte[] previous) throws IOException; }
    interface Storage { void transact(String host, Transaction transaction) throws IOException; }
    private final Storage storage;
    PinRepository(Storage storage) { this.storage = storage; }

    static final class Record {
        final String anchor, origin, app;
        final byte[] keyJson, pinsJson;
        final PinProtocol.Pins pins;
        Record(String anchor, String origin, String app, byte[] keyJson, byte[] pinsJson, String host) throws IOException {
            this.anchor = anchor; this.origin = origin; this.app = app;
            this.keyJson = keyJson.clone(); this.pinsJson = pinsJson.clone();
            try { pins = PinProtocol.pins(pinsJson, PinProtocol.key(keyJson, host, anchor)); }
            catch (IllegalArgumentException e) { throw new PinningException("invalid persisted anchor", e); }
        }
    }

    Record load(String host, String origin, String app, String anchor) throws IOException {
        Record[] result = new Record[1];
        storage.transact(host, previous -> {
            for (Record record : decode(previous, host).values()) {
                if (record.origin.equals(origin) && record.app.equals(app) && record.anchor.equals(anchor)) result[0] = record;
            }
            return previous;
        });
        return result[0];
    }

    void accept(String host, Record next, long now) throws IOException {
        next.pins.requireCurrent(now);
        storage.transact(host, previous -> {
            Map<String, Record> records = decode(previous, host);
            Record old = records.get(next.pins.key.environment);
            if (old != null) {
                PinProtocol.require(next.pins.version >= old.pins.version, "rollback rejected");
                PinProtocol.require(next.pins.version != old.pins.version || Arrays.equals(next.pins.canonical, old.pins.canonical), "same version has a different payload");
            }
            records.put(next.pins.key.environment, next);
            return encode(records);
        });
    }

    private Map<String, Record> decode(byte[] bytes, String host) throws IOException {
        Map<String, Record> records = new LinkedHashMap<>();
        if (bytes == null) return records;
        PinProtocol.require(bytes.length <= MAX_STORE, "oversized pin ledger");
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        PinProtocol.require(in.readInt() == 0x54505031, "invalid pin ledger");
        int count = in.readInt();
        PinProtocol.require(count >= 1 && count <= 32, "invalid pin ledger size");
        for (int i = 0; i < count; i++) {
            String anchor = in.readUTF(), origin = in.readUTF(), app = in.readUTF();
            Record record = new Record(anchor, origin, app, blob(in), blob(in), host);
            PinProtocol.require(records.put(record.pins.key.environment, record) == null, "duplicate ledger environment");
        }
        PinProtocol.require(in.read() == -1, "trailing ledger data");
        return records;
    }
    private byte[] blob(DataInputStream in) throws IOException {
        int length = in.readInt();
        PinProtocol.require(length > 0 && length <= PinProtocol.MAX_RESPONSE, "invalid ledger document size");
        byte[] bytes = new byte[length]; in.readFully(bytes); return bytes;
    }
    private byte[] encode(Map<String, Record> records) throws IOException {
        PinProtocol.require(records.size() <= 32, "too many ledger environments");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buffer);
        out.writeInt(0x54505031); out.writeInt(records.size());
        for (Record record : records.values()) {
            out.writeUTF(record.anchor); out.writeUTF(record.origin); out.writeUTF(record.app);
            out.writeInt(record.keyJson.length); out.write(record.keyJson);
            out.writeInt(record.pinsJson.length); out.write(record.pinsJson);
        }
        out.flush();
        PinProtocol.require(buffer.size() <= MAX_STORE, "pin ledger full");
        return buffer.toByteArray();
    }
}
