package com.protocol.resp;

public class RespInteger implements RespType {

    long value;

    public RespInteger(long value){
        this.value = value;
    }

}
