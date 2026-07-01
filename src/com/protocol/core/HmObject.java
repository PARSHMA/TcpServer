package com.protocol.core;

public class HmObject {

    Object value;
    long expiresAt;

    public HmObject(Object value, long expiresAt) {
        this.value = value;
        this.expiresAt = expiresAt;
    }
}
