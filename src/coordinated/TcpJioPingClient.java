package coordinated;
/*
 * Heavily inspired by http://code.google.com/p/core-java-performance-examples/source/browse/trunk/src/test/java/com/google/code/java/core/socket/PingTest.java
 * And therefore maintaining original licence:
 * -------------------------------------------
 * Copyright (c) 2011.  Peter Lawrey
 *
 * "THE BEER-WARE LICENSE" (Revision 128)
 * As long as you retain this notice you can do whatever you want with this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return
 * There is no warranty.
 * -------------------------------------------
 * Further mutated by Nitsan Wakart.
 */

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

public class TcpJioPingClient extends TcpPingClient {

    private Socket socket;
    private WritableByteChannel wrapped;


    public TcpJioPingClient(String[] args) throws IOException, InterruptedException {
        super(args);
    }

    @Override
    void initChannel() throws IOException {
        socket = new Socket(host, port);
        socket.setTcpNoDelay(true);
        wrapped = Channels.newChannel(socket.getOutputStream());

    }

    @Override
    void cleanup() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    void ping(ByteBuffer bb) throws IOException {
        // send
        bb.position(0);
        bb.limit(messageSize);
        do {
            wrapped.write(bb);
        } while (bb.hasRemaining());

        // receive
        bb.clear();
        int bytesRead = 0;
        do {
            bytesRead += channel.read(bb);
        } while (bytesRead < messageSize);
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        new TcpSpinPingClient(args);
    }
}
