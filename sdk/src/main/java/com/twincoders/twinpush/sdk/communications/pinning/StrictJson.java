package com.twincoders.twinpush.sdk.communications.pinning;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small bounded parser for protocol objects: preserves integer types and rejects duplicates. */
final class StrictJson {
    private final String text;
    private int pos;
    private StrictJson(String text) { this.text = text; }

    static Map<String, Object> object(byte[] bytes, int limit) throws PinningException {
        if (bytes.length > limit) throw bad();
        try {
            String text = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();
            StrictJson parser = new StrictJson(text);
            Object result = parser.value(0);
            parser.space();
            if (parser.pos != text.length() || !(result instanceof Map)) throw bad();
            return (Map<String, Object>) result;
        } catch (CharacterCodingException e) { throw bad(); }
    }

    private Object value(int depth) throws PinningException {
        if (depth > 4) throw bad();
        space();
        char c = peek();
        if (c == '"') return string();
        if (c == '{') {
            pos++;
            Map<String, Object> result = new LinkedHashMap<>();
            space();
            if (take('}')) return result;
            do {
                space();
                String name = string();
                if (result.containsKey(name) || result.size() >= 16) throw bad();
                space(); expect(':');
                result.put(name, value(depth + 1));
                space();
                if (take('}')) return result;
                expect(',');
            } while (true);
        }
        if (c == '[') {
            pos++;
            List<Object> result = new ArrayList<>();
            space();
            if (take(']')) return result;
            do {
                if (result.size() >= 32) throw bad();
                result.add(value(depth + 1));
                space();
                if (take(']')) return result;
                expect(',');
            } while (true);
        }
        // Protocol v1 contains only positive integers. Fractions/exponents/signs are not integers.
        int start = pos;
        while (pos < text.length() && text.charAt(pos) >= '0' && text.charAt(pos) <= '9') pos++;
        if (start == pos || pos - start > 16 || (pos - start > 1 && text.charAt(start) == '0')) throw bad();
        try { return Long.valueOf(text.substring(start, pos)); }
        catch (NumberFormatException e) { throw bad(); }
    }

    private String string() throws PinningException {
        expect('"');
        StringBuilder result = new StringBuilder();
        while (pos < text.length()) {
            char c = text.charAt(pos++);
            if (c == '"') return result.toString();
            if (c < 32) throw bad();
            if (c == '\\') {
                c = peek(); pos++;
                switch (c) {
                    case '"': case '\\': case '/': break;
                    case 'b': c = '\b'; break;
                    case 'f': c = '\f'; break;
                    case 'n': c = '\n'; break;
                    case 'r': c = '\r'; break;
                    case 't': c = '\t'; break;
                    case 'u':
                        if (pos + 4 > text.length()) throw bad();
                        String hex = text.substring(pos, pos + 4);
                        if (!hex.matches("[0-9a-fA-F]{4}")) throw bad();
                        c = (char) Integer.parseInt(hex, 16); pos += 4; break;
                    default: throw bad();
                }
            }
            result.append(c);
        }
        throw bad();
    }
    private void space() { while (pos < text.length() && " \t\r\n".indexOf(text.charAt(pos)) >= 0) pos++; }
    private char peek() throws PinningException { if (pos >= text.length()) throw bad(); return text.charAt(pos); }
    private boolean take(char c) { if (pos < text.length() && text.charAt(pos) == c) { pos++; return true; } return false; }
    private void expect(char c) throws PinningException { if (!take(c)) throw bad(); }
    private static PinningException bad() { return new PinningException("invalid or oversized protocol JSON"); }
}
