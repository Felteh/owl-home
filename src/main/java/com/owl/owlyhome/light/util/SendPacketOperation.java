package com.owl.owlyhome.light.util;

import java.util.Arrays;

public class SendPacketOperation implements Operation {

    public final byte[] bytes;

    public SendPacketOperation(byte... bytes) {
        this.bytes = bytes;
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.SEND_PACKET;
    }

    @Override
    public String toString() {
        return "SendPacketOperation{" + "bytes=" + Arrays.toString(bytes) + '}';
    }

}
