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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import util.Helper;

public class TcpJioPingServer {
    private static final int PAGE_SIZE = 4096;

    public static void main(String[] args) throws IOException, InterruptedException {
        String nic = args.length > 0 ? args[0] : "0.0.0.0";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 12345;
        System.out.println("Listening on interface : " + nic + ":" + port);
        final ByteBuffer buffy = ByteBuffer.allocateDirect(PAGE_SIZE).order(ByteOrder.nativeOrder());
        final ServerSocket server = new ServerSocket();
        server.bind(new InetSocketAddress(nic, port));
        Socket accepted = null;
        try {
            accepted = server.accept();
            accepted.setTcpNoDelay(true);
            server.close();
            final WritableByteChannel write = Channels.newChannel(accepted.getOutputStream());
            final ReadableByteChannel read = Channels.newChannel(accepted.getInputStream());

            while (!Thread.interrupted()) {
                if (pong(buffy, write, read)) return;
            }
        } finally {
            if (accepted != null) {
                try {
                    accepted.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static boolean pong(
        ByteBuffer buffy,
        WritableByteChannel writeable, ReadableByteChannel readable
        ) throws IOException {
        int read;
        buffy.clear();
        while ((read = readable.read(buffy)) == 0) {
            Helper.yield();
        }

        if (read == -1)
            return true;
        buffy.flip();
        do {
            writeable.write(buffy);
        } while (buffy.hasRemaining());
        return false;
    }
}
