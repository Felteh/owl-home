package com.owl.owlyhome.light.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightUtil {

    private static final Logger LOG = LoggerFactory.getLogger(LightUtil.class);

    private static final int PORT = 8899;

    public static CompletableFuture<Void> executeCommand(InetAddress inetAddress, DatagramSocket socket, OperationList a) throws IOException, InterruptedException {
        return CompletableFuture.runAsync(() -> {
            try {
                for (Operation o : a.operations) {
                    switch (o.getOperationType()) {
                        case SLEEP:
                            LOG.debug("Sleeping");
                            sleep((SleepOperation) o);
                            break;
                        case SEND_PACKET:
                            LOG.debug("Sending packet");
                            sendPacket(inetAddress, socket, (SendPacketOperation) o);
                            break;
                        default:
                            throw new RuntimeException("No operation type known");
                    }
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException("LightUtil failure", e);
            }
        });
    }

    public static void sendPacket(InetAddress inetAddress, DatagramSocket socket, SendPacketOperation a) throws IOException {
        LOG.debug("UDP SEND={}", a.toString());
        DatagramPacket packet = new DatagramPacket(a.bytes, a.bytes.length, inetAddress, PORT);
        socket.send(packet);
    }

    public static void sleep(SleepOperation sleepOperation) throws InterruptedException {
        LOG.debug("OP: " + sleepOperation.toString());
        Thread.sleep(sleepOperation.sleepLengthMillis);
    }
}
