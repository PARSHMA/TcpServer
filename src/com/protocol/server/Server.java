package com.protocol.server;

import com.protocol.core.Eval;
import com.protocol.core.Rediscmd;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class Server {

    public void runTcpServer(String host, int port) throws Exception {

        System.out.println("starting a synchronous TCP server on" + " " + host + " " + port);

        int con_clients  = 0;

      ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        server.configureBlocking(true); // blocking calls
        while (true) {
            SocketChannel channel = server.accept();
            con_clients +=1;
            System.out.println("clinet connected with address: " + channel.getRemoteAddress() + ", " + "concurrentclients: "+con_clients);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
       while(true){
         //over the socket, continously read the commmand and print it out

           int bytesRead = channel.read(buffer);

           if (bytesRead == -1) {
               channel.close();
               con_clients -= 1;
               System.out.println("Client disconnected");
               break; // client closed connection
           }
           Rediscmd redisCmd =  readCommand(buffer);
           respond(redisCmd, channel);
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
        Eval eval = new Eval();
        eval.evalAndRespond(cmd, channel);
    }


}
