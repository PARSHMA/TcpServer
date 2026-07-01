package com.protocol.resp;

public class RespSimpleString implements RespType {

    public RespSimpleString(String value){
        this.value = value;
    }

    String value;

    public String getValue() {
        return value;
    }
}
