package com.protocol.resp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Resp {

    public RespType Decode(byte[] data) throws Exception {
        if (data.length == 0) {
            throw new Exception("no data");
        }
       DecodeOneResult result = DecodeOne(data);
        return result.getValue();
    }

    public DecodeOneResult DecodeOne(byte[] data) {
        byte prefix = data[0];
        switch (prefix) {
            case '+':
                return readSimpleString(data);
            case ':':
                return readInteger(data);
            case '$':
                return readBulkString(data);
            case '*':
                return readArray(data);
            //case '-': this for decoding errors

        }

        return new DecodeOneResult();
    }

    public DecodeOneResult readSimpleString(byte[] data) {
        int pos = 1;
        for (; data[pos] != '\r'; pos++) {

        }
        String result = new String(data, 1, pos - 1, StandardCharsets.UTF_8);
        RespSimpleString respSimpleString = new RespSimpleString(result);
        return new DecodeOneResult(respSimpleString, pos);
    }

    public DecodeOneResult readInteger(byte[] data) {
        int pos = 1;
        long value = 0;

        while (data[pos] != '\r') {
            value = value * 10 + (data[pos] - '0');
            pos++;
        }
        RespInteger respInteger = new RespInteger(value);
        return new DecodeOneResult(respInteger, pos + 2);
    }

    public DecodeOneResult readBulkString(byte[] data) {
        int pos = 1;
        int arr[] = readLength(data);
        pos += arr[1];
        String result = new String(data, pos , arr[0], StandardCharsets.UTF_8);
        RespSimpleString respSimpleString = new RespSimpleString(result);
        return new DecodeOneResult(respSimpleString, pos+arr[0]+2);
    }

    int[] readLength(byte[] data){
        int[] res = new int[2];
        int pos = 1;
        int length = 0;
        while (data[pos] >= '0' && data[pos]<= '9') {
            length = length * 10 + (data[pos]-'0');
            pos++;
        }
        if(pos>1){
            res[0] = length;
            res[1] = pos;
            return res;
        }
        return res;
    }

    public DecodeOneResult readArray(byte[] data){
         int pos = 1;
         int arr[] = readLength(data);
         pos += arr[1];

        List<RespType> elements = new ArrayList<>();
         for(int i = 0 ; i < arr[0]; i++){
             DecodeOneResult result =DecodeOne(Arrays.copyOfRange(data, pos, data.length-1));
             elements.add(result.getValue());
             pos  += result.getBytesRead();
         }
         RespArray array = new RespArray(elements);
         DecodeOneResult result = new DecodeOneResult(array, pos);
         return result;
    }

    public String[] DecodeArrayFromString(byte[] data) throws Exception {
        RespArray type = (RespArray) Decode(data);
        List<RespType> list = type.getValues();
        String[] arr = new String[list.size()];
        for(int i = 0 ; i < list.size();i++){
          RespSimpleString str = (RespSimpleString) list.get(i);
            arr[i] = str.getValue();
        }
        return arr;
    }


}

    
