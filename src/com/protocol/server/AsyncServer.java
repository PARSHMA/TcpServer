package com.protocol.server;
import com.protocol.core.Eval;
import com.protocol.core.Expire;
import com.protocol.core.Rediscmd;
import com.protocol.resp.Resp;
import com.protocol.resp.RespArray;
import com.protocol.resp.RespSimpleString;
import com.protocol.resp.RespType;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AsyncServer {

    private final Eval eval = new Eval();

    private final Expire  expire = new Expire();

    Duration cronFrequency = Duration.ofSeconds(1);
    Instant lastCronExecTime = Instant.now();

    public void runAsyncTcpServer(String host, int port) throws Exception {
        Selector selector = Selector.open();

        int con_clients  = 0;

        ServerSocketChannel serverChannel = ServerSocketChannel.open();

        serverChannel.bind(new InetSocketAddress(9000));

        serverChannel.configureBlocking(false);

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port 9000");

        while (true) {

            if(Instant.now().isAfter(lastCronExecTime.plus(cronFrequency))){
                expire.activeExpireCycle();
                lastCronExecTime = Instant.now();
            }
            selector.select();

            Iterator<SelectionKey> keys =
                    selector.selectedKeys().iterator();

            while (keys.hasNext()) {

                SelectionKey key = keys.next();



                if (key.isAcceptable()) {

                    ServerSocketChannel server =
                            (ServerSocketChannel) key.channel();

                    SocketChannel client = server.accept();

                    client.configureBlocking(false);

                    client.register(selector, SelectionKey.OP_READ);
                    con_clients +=1;

                    System.out.println("Client connected, no of concurrent clients: " + con_clients);
                }

                else if (key.isReadable()) {

                    SocketChannel client =
                            (SocketChannel) key.channel();

                    ByteBuffer buffer =
                            ByteBuffer.allocate(1024);

                    int bytesRead = client.read(buffer);

                    if (bytesRead == -1) {
                        con_clients -=1;
                        client.close();
                        System.out.println("Client disconnected, no of concurrent clients: " + con_clients);
                        continue;
                    }

                    Rediscmd[] redisCmd =  readCommand(buffer);
                    respond(redisCmd, client);
                }
                keys.remove();
            }
        }
    }

    /*public Rediscmd readCommand(ByteBuffer buffer) throws Exception {
        buffer.flip();

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        Resp resp = new Resp();
        RespType respType = resp.Decode(data);

        if (!(respType instanceof RespArray)) {
            throw new IllegalArgumentException("Expected RESP Array");
        }

        RespArray respArray = (RespArray) respType;
        List<RespType> values = respArray.getValues();

        if (values.isEmpty()) {
            throw new IllegalArgumentException("Empty command");
        }

        Rediscmd redisCmd = new Rediscmd();

        // First element is the command
        redisCmd.setCmd(getStringValue(values.get(0)));

        // Remaining elements are arguments
        String[] args = new String[values.size() - 1];
        for (int i = 1; i < values.size(); i++) {
            args[i - 1] = getStringValue(values.get(i));
        }
        if(args.length == 0){
            redisCmd.setArgs(null);
        } else {
            redisCmd.setArgs(args);
        }

        buffer.clear();
        return redisCmd;
    }*/

    public Rediscmd[] readCommand(ByteBuffer buffer) throws Exception {
        buffer.flip();

        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        Resp resp = new Resp();
        List<RespType> respTypeList = resp.Decode(data);

        Rediscmd[] redisCmds = new Rediscmd[respTypeList.size()];

        for(int i = 0 ; i < respTypeList.size(); i++){
            RespType respType =  respTypeList.get(i);

            if (!(respType instanceof RespArray)) {
                throw new IllegalArgumentException("Expected RESP Array");
            }

            RespArray respArray = (RespArray) respType;

            List<RespType> values = respArray.getValues();

            if (values.isEmpty()) {
                throw new IllegalArgumentException("Empty command");
            }

            Rediscmd redisCmd = new Rediscmd();
                // First element is the command
            redisCmd.setCmd(getStringValue(values.get(0)));
            // Remaining elements are arguments
            String[] args = new String[values.size() - 1];
            for(int j = 1 ; j < values.size(); j++) {
                args[j - 1] = getStringValue(values.get(j));
            }

            if (args.length == 0) {
                redisCmd.setArgs(null);
            } else {
                redisCmd.setArgs(args);
            }
            redisCmds[i] = redisCmd;
        }
        buffer.clear();
        return redisCmds;
    }

    private String getStringValue(RespType value) {
      /*  if (value instanceof RespBulkString) {
            return ((RespBulkString) value).getValue();
        }*/


        if (value instanceof RespSimpleString) {
            return ((RespSimpleString) value).getValue();
        }

        throw new IllegalArgumentException(
                "Unsupported RESP type: " + value.getClass().getSimpleName()
        );
    }

    public void respond(Rediscmd[] cmds, SocketChannel channel) throws Exception {
        eval.evalAndRespond(cmds, channel);
    }
}
