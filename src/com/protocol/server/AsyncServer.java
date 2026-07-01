package com.protocol.server;
import com.protocol.core.Eval;
import com.protocol.core.Expire;
import com.protocol.core.Rediscmd;

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

                    Rediscmd redisCmd =  readCommand(buffer);
                    respond(redisCmd, client);
                }
                keys.remove();
            }
        }
    }

    public Rediscmd readCommand(ByteBuffer buffer) throws Exception {
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        //Resp resp = new Resp();
        //String[] arr = resp.DecodeArrayFromString(data);
        String str = new String(data, StandardCharsets.UTF_8).trim();
        String[] arr = str.split(" ");

        Rediscmd redisCmd = new Rediscmd();
        redisCmd.setCmd(arr[0]);
        if(arr.length > 1){
            redisCmd.setArgs(Arrays.copyOfRange(arr, 1, arr.length));
        }

        buffer.clear();
        return redisCmd;
    }

    public void respond(Rediscmd cmd, SocketChannel channel) throws Exception {
        eval.evalAndRespond(cmd, channel);
    }
}
