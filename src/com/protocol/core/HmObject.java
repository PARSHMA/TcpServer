package com.protocol.core;

public class HmObject {

     byte typeEncoding;
    Object value;
    long expiresAt;

    public byte getType() {
        return (byte) (typeEncoding & 0xF0);
    }

    public byte getEncoding() {
        return (byte) (typeEncoding & 0x0F);
    }

    public void setType(byte type) {
        typeEncoding = (byte) (type | getEncoding());
    }

    public void setEncoding(byte encoding) {
        typeEncoding = (byte) (getType() | encoding);
    }


    public HmObject(Object value, long expiresAt, byte typeEncoding) {
        this.value = value;
        this.expiresAt = expiresAt;
        this.typeEncoding = typeEncoding;
    }
}
