package com.owl.owlyhome.light;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import akka.pattern.PatternsCS;
import com.owl.owlyhome.light.util.Commands;
import com.owl.owlyhome.light.util.LightUtil;
import com.owl.owlyhome.light.util.OperationList;
import com.owl.owlyhome.light.util.SendPacketOperation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class LightActor extends AbstractActor {

    private final LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);
    public static final String ON = "On";
    public static final String OFF = "Off";

    public static final String SUCCESS = "Success";

    private State state = State.OFF;

    public final static Props props(LightGroup group) {
        return Props.create(LightActor.class, () -> new LightActor(group));
    }

    private final LightGroup group;
    private final InetAddress inetAddress;
    private final DatagramSocket socket;

    public LightActor(LightGroup group) throws IOException {
        this.group = group;
        inetAddress = InetAddress.getByName(group.ipAddress);

        socket = connect();
    }


    @Override
    public Receive createReceive() {
        return ReceiveBuilder
                .create()
                .match(
                        String.class,
                        (s) -> ON.equals(s),
                        (s) -> on()
                )
                .match(
                        String.class,
                        (s) -> OFF.equals(s),
                        (s) -> off()
                )
                .matchAny((s) -> LOG.debug("Peculiar input={}", s))
                .build();
    }

    private DatagramSocket connect() throws IOException {
        LOG.debug("Connecting to light server");
        try (DatagramSocket connectSocket = new DatagramSocket()) {
            final int connectPort = 48899;
            byte[] buf = "Link_Wi-Fi".getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, inetAddress, connectPort);
            LOG.debug("UDP SEND={}", Arrays.toString(buf));
            connectSocket.send(packet);

            byte[] receiveData = new byte[32];
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);
            boolean found = false;
            while (!found) {
                connectSocket.receive(receivePacket);
                String expectedSentence = group.ipAddress + "," + group.macAddress.toUpperCase().replace(":", "") + ",";
                String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
                LOG.debug("UDP RECEIVED={} EXPECTED={}", sentence, expectedSentence);
                if (expectedSentence.equals(sentence)) {
                    found = true;
                    LOG.debug("UDP LINKED");
                }
            }
        }

        return new DatagramSocket();
    }

    private void on() throws IOException, InterruptedException {
        PatternsCS
                .pipe(
                        LightUtil
                                .executeCommand(
                                        inetAddress, socket, new OperationList().withOps(new SendPacketOperation(Commands.RGBW_COLOR_LED_ALL_ON.toByteArray()))
                                )
                                .thenApply((v) -> {
                                    return success(State.OFF);
                                }),
                        context().dispatcher()
                )
                .to(sender());
    }

    private void off() throws IOException, InterruptedException {
        PatternsCS
                .pipe(
                        LightUtil
                                .executeCommand(
                                        inetAddress, socket, new OperationList().withOps(new SendPacketOperation(Commands.RGBW_COLOR_LED_ALL_OFF.toByteArray()))
                                )
                                .thenApply((v) -> {
                                    return success(State.OFF);
                                }),
                        context().dispatcher()
                )
                .to(sender());
    }

    private String success(State newState) {
        state = newState;
        return SUCCESS;
    }

    private static enum State {
        ON,
        OFF
    }

}
