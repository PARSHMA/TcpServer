package com.protocol.resp;

public class DecodeOneResult {

    public RespType value;
    public  int bytesRead;

    public int getBytesRead() {
        return bytesRead;
    }

    public RespType getValue() {
        return value;
    }

    public DecodeOneResult(RespType value, int bytesRead) {
        this.value = value;
        this.bytesRead = bytesRead;
    }

    public DecodeOneResult(){

    }


}