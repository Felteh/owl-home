package com.owl.owlyhome.light;

public enum LightGroup {
    LoftLounge("192.168.1.70", "ac:cf:23:3e:10:56", "tv", "Loft Lounge");
    public final String ipAddress;
    public final String macAddress;
    public final String icon;
    public final String displayName;

    private LightGroup(String ipAddress, String macAddress, String icon, String displayName) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.icon = icon;
        this.displayName = displayName;
    }

    public static LightGroup fromMacAddress(String macAddress) {
        for (LightGroup l : values()) {
            if (l.macAddress.equals(macAddress)) {
                return l;
            }
        }
        throw new RuntimeException("Could not find LightGroup for macAddress: " + macAddress);
    }

}
