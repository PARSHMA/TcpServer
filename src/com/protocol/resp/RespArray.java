package com.protocol.resp;

import java.util.List;

public class RespArray implements RespType {

    List<RespType> values;

    public List<RespType> getValues() {
        return values;
    }

    public RespArray(List<RespType> values){
        this.values = values;
    }
}
