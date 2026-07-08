package com.protocol.common;

import com.protocol.core.HmObject;

public final class ObjectUtils {

    private ObjectUtils() {
    }

    public static void assertType(HmObject obj, byte expectedType) {
        if (obj.getType() != expectedType) {
            throw new IllegalStateException(
                    "WRONGTYPE Operation against a key holding the wrong kind of value");
        }
    }

    public static void assertEncoding(HmObject obj, byte expectedEncoding) {
        if (obj.getEncoding() != expectedEncoding) {
            throw new IllegalStateException(
                    "The operation is not permitted on this encoding"
            );
        }
    }

    public static byte[] deduceTypeEncoding(String value) {

        byte type = RedisConstants.OBJ_TYPE_STRING;

        // Check if the value is an integer
        try {
            Long.parseLong(value);
            return new byte[] { type, RedisConstants.OBJ_ENCODING_INT };
        } catch (NumberFormatException ignored) {
            // Not an integer, continue
        }

        if (value.length() <= 44) {
            return new byte[] { type, RedisConstants.OBJ_ENCODING_EMBSTR };
        }

        return new byte[] { type, RedisConstants.OBJ_ENCODING_RAW };
    }
}