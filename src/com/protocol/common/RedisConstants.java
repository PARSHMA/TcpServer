package com.protocol.common;

public final class RedisConstants {

    private RedisConstants() {}

    // Types
    public static final byte OBJ_TYPE_STRING = (byte) (0 << 4);


    // Encodings
    public static final byte OBJ_ENCODING_RAW    = 0;
    public static final byte OBJ_ENCODING_INT    = 1;
    public static final byte OBJ_ENCODING_EMBSTR = 8;
}