package com.owl.owlyhome.light.util;

public enum Commands {

    RGBW_COLOR_LED_ALL_OFF(0x41, 65),
    RGBW_COLOR_LED_ALL_ON(0x42, 66),
    DISCO_SPEED_SLOWER(0x43, 67),
    DISCO_SPEED_FASTER(0x44, 68),
    GROUP_1_ALL_ON(0x45, 69),
    GROUP_1_ALL_OFF(0x46, 70),
    GROUP_2_ALL_ON(0x47, 71),
    GROUP_2_ALL_OFF(0x48, 72),
    GROUP_3_ALL_ON(0x49, 73),
    GROUP_3_ALL_OFF(0x4A, 74),
    GROUP_4_ALL_ON(0x4B, 75),
    GROUP_4_ALL_OFF(0x4C, 76),
    DISCO_MODE(0x4D, 77);

    public final Integer bite;
    public final Integer dec;

    private Commands(Integer bite, Integer dec) {
        this.bite = bite;
        this.dec = dec;
    }

    public byte[] toByteArray(){
        byte[] byteArray=  {bite.byteValue(),0x00,0x55};
        return byteArray;
    }
    
}
