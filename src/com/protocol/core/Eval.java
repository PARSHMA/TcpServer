package com.protocol.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Eval {

    private final Store store = new Store();

    public void evalAndRespond(Rediscmd[] cmds, SocketChannel channel) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for(Rediscmd cmd : cmds) {
            switch (cmd.getCmd()) {
                case "PING":
                    out.write(evalPing(cmd.getArgs()));
                    break;
                case "SET":
                    out.write(evalSet(cmd.getArgs()));
                    break;
                case "GET":
                    out.write(evalGet(cmd.getArgs()));
                    break;
                case "TTL":
                    out.write(evalTtl(cmd.getArgs()));
                    break;
                case "DEL":
                    out.write(evalDel(cmd.getArgs()));
                    break;
                case "EXPIRE":
                    out.write(evalExpire(cmd.getArgs()));
                    break;
            }
        }
        ByteBuffer buffer = ByteBuffer.wrap(out.toByteArray());

        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    public byte[] evalPing(String[] args) throws Exception {
        byte[] b;

      if(args == null ){
           b =encode("PONG", true);
      }else if(args.length >= 2){
          String v = "ERR wrong number of arguments for ping command";
           b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
      }else{
          b = encode(args[0], false);
      }
        return b;
    }

    public byte[]  evalSet(String[] args) throws Exception{
        byte[] b = new byte[512];
        String v = null;
        if(args.length <= 1){
            v = "(error) ERR wrong number of arguments for 'set' command";
            b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
          } else {
            String key = args[0];
            String value = args[1];
            int exDurationMs = -1;
            for(int i=2; i < args.length; i++ ){
                switch(args[i]){
                    case "EX":
                        i++;
                        if(i == args.length){
                            v = "(error) ERR syntax error";
                            b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
                        }else{
                            try {
                                int exDurationSec = Integer.parseInt(args[3]);
                                exDurationMs = exDurationSec * 1000;
                            } catch(NumberFormatException e){
                                v = "(error) ERR value is not an integer or out of range";
                                b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
                            }
                        }
                        break;

                    default:
                        v = "(error) ERR syntax error";
                        b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
                }
            }
            if(v == null){
                store.set(key, value,exDurationMs);
                 b =  ("+OK\r\n").getBytes(StandardCharsets.UTF_8);
            }
        }
        return b;
    }

    public byte[] evalGet(String[] args) throws IOException {
        byte[] b = new byte[512];
        String v = null;
       if(args.length != 1){
           v = "(error) ERR wrong number of arguments for 'set' command";
           b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
       } else {
           String key = args[0];
           HmObject obj = store.get(key);
           if(obj == null){
               b = ("$-1\r\n").getBytes(StandardCharsets.UTF_8);
           } else if(obj.expiresAt != -1 && obj.expiresAt <= System.currentTimeMillis()){
               b = ("$-1\r\n").getBytes(StandardCharsets.UTF_8);
               //RESPNIL ("$-1\r\n") fomat
           } else {
               b = encode(obj.value, false);
           }
       }
        return b;
    }

    public byte[]  evalTtl(String[] args) throws IOException {
        byte[] b = new byte[512];
        String v = null;
        if(args.length != 1){
            v = "(error) ERR wrong number of arguments for 'TTL' command";
            b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
        } else {
            String key = args[0];
            HmObject obj = store.get(key);
            //if key does not exist, return RESP encoded -2 denoting key does not exist
            if (obj == null) {
                b = ("$-2\r\n").getBytes(StandardCharsets.UTF_8);
            } else if (obj.expiresAt == -1) { // if object exist , but no expiration is set on it  then send -1
                b = ("$-1\r\n").getBytes(StandardCharsets.UTF_8);
            } else if (obj.expiresAt - System.currentTimeMillis() < 0) { // compute the time remaining for the key to expire
                                                                         // and return the RESP encoded form of it
                b = ("$-2\r\n").getBytes(StandardCharsets.UTF_8);   //if key is expired i.e key does not exist hence return -1
            } else {
                b = encode((int)((obj.expiresAt - System.currentTimeMillis())/1000), false);
            }
        }
        return b;
    }

    public byte[] evalDel(String[] args) throws IOException {
        byte[] b = new byte[512];
        int countDeleted = 0;
        for(String key : args){
            if(store.delete(key)){
                countDeleted++;
            }
        }
        b =  encode(countDeleted, false);
        return b;
    }

    public byte[] encode(Object value, boolean isSimple) {
        if (value instanceof String) {
            String v = (String) value;

            if (isSimple) {
                return ("+" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
            }

            return ("$" + v.length() + "\r\n" + v + "\r\n")
                    .getBytes(StandardCharsets.UTF_8);
        } else if (value instanceof Integer){
            Integer v = (Integer) value;
            return (":" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
        } else {
            return  ("$-1\r\n").getBytes(StandardCharsets.UTF_8);
        }
    }

    public byte[] evalExpire(String[] args) throws IOException {
        byte[] b = new byte[512];
        String v = null;
        int exDurationSec = 0;
        if(args.length <= 1){
            v = "(error) ERR wrong number of arguments for 'expire' command";
            b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
        } else {
            String key = args[0];
            try {
                 exDurationSec = Integer.parseInt(args[1]);
            } catch(NumberFormatException e){
                v = "(error) ERR value is not an integer or out of range";
                b =  ("-" + v + "\r\n").getBytes(StandardCharsets.UTF_8);
                return b;
            }
            HmObject obj = store.get(key);
            //0 if the timeout was not set e.g. hey key doesn't exist, or operation skipped due to
            // the provided argument
            if (obj == null) {
                b = (":0\r\n").getBytes(StandardCharsets.UTF_8);
            } else {
                obj.expiresAt =  System.currentTimeMillis()  + exDurationSec*1000;
                b = (":1\r\n").getBytes(StandardCharsets.UTF_8);
            }
        }
        return b;
    }
}
