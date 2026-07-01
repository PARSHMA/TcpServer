import com.protocol.server.AsyncServer;
import com.protocol.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * use this command to make it up:  ncat localhost 8080
 */
public class Main {
    public static void main(String[] args) throws Exception {

        String host = args.length > 0 ? args[0]: "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]):8080;

        /*Server server = new Server();
        server.runTcpServer(host, port);*/
        AsyncServer server = new AsyncServer();
        server.runAsyncTcpServer(host, port);
        //runTcpServer(host, port);
    }

    public static void runTcpServer(String host, int port) throws IOException {

        System.out.println("starting a synchronous TCP server on" + " " + host + " " + port);

        int con_clients  = 0;

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(host, port));
       // server.configureBlocking(true); // blocking calls
        while (true) {
            SocketChannel channel = server.accept();
            con_clients +=1;
            System.out.println("client connected with address: " + channel.getRemoteAddress() + ", " + "concurrentclients: "+con_clients);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(true){
                //over the socket, continuously read the command and print it out

                int bytesRead = channel.read(buffer);

                if (bytesRead == -1) {
                    channel.close();
                    con_clients -= 1;
                    System.out.println("Client disconnected, concurrentclients: " + con_clients);
                    break; // client closed connection
                }

                buffer.flip();              // switch to read mode
                channel.write(buffer);      // send back (echo)
                buffer.clear();
            }

            /*channel.close();
            con_clients -= 1;*/
            //System.out.println("Client disconnected, concurrentclients: " + con_clients);
        }

    }
}