package com.protocol.resp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Resp {

    public List<RespType> Decode(byte[] data) throws Exception {
        if (data.length == 0) {
            throw new Exception("no data");
        }
        List<RespType> list = new ArrayList<>();
        int index = 0;
        while(index < data.length){
            DecodeOneResult result = DecodeOne(data, index);
            list.add(result.getValue());
            index += result.getBytesRead();
        }
        return list;
    }

    public DecodeOneResult DecodeOne(byte[] data, int offset) {
        byte prefix = data[offset];
        switch (prefix) {
            case '+':
                return readSimpleString(data, offset);
            case ':':
                return readInteger(data, offset);
            case '$':
                return readBulkString(data, offset);
            case '*':
                return readArray(data, offset);
            //case '-': this for decoding errors

        }

        return new DecodeOneResult();
    }

    public DecodeOneResult readSimpleString(byte[] data, int offset) {
        int pos =  offset + 1;
        for (; data[pos] != '\r'; pos++) {

        }
        String result = new String(data, offset + 1, pos-offset - 1, StandardCharsets.UTF_8);
        RespSimpleString respSimpleString = new RespSimpleString(result);
        return new DecodeOneResult(respSimpleString, pos+2-offset);
    }

    public DecodeOneResult readInteger(byte[] data, int offset) {
        int pos = offset + 1;
        long value = 0;

        while (data[pos] != '\r') {
            value = value * 10 + (data[pos] - '0');
            pos++;
        }
        RespInteger respInteger = new RespInteger(value);
        return new DecodeOneResult(respInteger, pos + 2-offset);
    }

    public DecodeOneResult readBulkString(byte[] data, int offset) {

        int arr[] = readLength(data, offset);
        int pos = arr[1];
        String result = new String(data, pos , arr[0], StandardCharsets.UTF_8);
        RespSimpleString respSimpleString = new RespSimpleString(result);
        return new DecodeOneResult(respSimpleString, pos+arr[0]+2-offset);
    }

    int[] readLength(byte[] data, int offset){
        int[] res = new int[2];
        int pos = offset + 1;
        int length = 0;
        while (data[pos] >= '0' && data[pos]<= '9') {
            length = length * 10 + (data[pos]-'0');
            pos++;
        }
        if(pos> offset + 1){
            res[0] = length;
            res[1] = pos + 2;
            return res;
        }
        return res;
    }

   public DecodeOneResult readArray(byte[] data, int offset){

         int arr[] = readLength(data, offset);
         int count = arr[0];
         int pos = arr[1];

        List<RespType> elements = new ArrayList<>();
         for(int i = 0 ; i < count; i++){
             DecodeOneResult result = DecodeOne(data, pos);
             elements.add(result.getValue());
             pos += result.getBytesRead();
         }
       return new DecodeOneResult(
               new RespArray(elements),
               pos - offset);
    }

    /*public DecodeOneResult readArray(byte[] data) {

        int pos = 1;

        int[] arr = readLength(data);

        System.out.println("Array length = " + arr[0]);
        System.out.println("Header bytes = " + arr[1]);

        pos = arr[1];

        List<RespType> elements = new ArrayList<>();

        for (int i = 0; i < arr[0]; i++) {

            System.out.println("\n========== ELEMENT " + i + " ==========");
            System.out.println("Current pos = " + pos);

            byte[] slice = Arrays.copyOfRange(data, pos, data.length);

            System.out.println("Remaining buffer:");
            System.out.println(new String(slice, StandardCharsets.UTF_8));

            System.out.println("First byte = " + (char) slice[0]);

            DecodeOneResult r = DecodeOne(slice);

            System.out.println("Returned object = " + r.getValue());

            if (r.getValue() == null) {
                System.out.println("ERROR: DecodeOne returned NULL");
            }

            elements.add(r.getValue());

            System.out.println("Bytes read = " + r.getBytesRead());

            pos += r.getBytesRead();
        }

        RespArray array = new RespArray(elements);

        DecodeOneResult result = new DecodeOneResult(array, pos);

        System.out.println("\n========== FINAL ARRAY ==========");
        System.out.println("Size = " + array.getValues().size());

        for (int i = 0; i < array.getValues().size(); i++) {

            RespType t = array.getValues().get(i);

            System.out.println("Element " + i + " = " + t);

            if (t instanceof RespSimpleString) {
                System.out.println("Value = " + ((RespSimpleString) t).getValue());
            }
        }

        return result;
    }*/

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

    
