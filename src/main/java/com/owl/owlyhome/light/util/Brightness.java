package com.owl.owlyhome.light.util;

public class Brightness {

    public static final Integer MIN = 0;
    public static final Integer MAX = 27;

    public static byte[] toByteArray(Integer brightness) {
        byte[] byteArray = {0x4E, brightness.byteValue(), 0x55};
        return byteArray;
    }
}
