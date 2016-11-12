package com.owl.owlyhome.light;

public class Light {

    public final String id;
    public final String icon;
    public final String name;
    public final boolean enabled;

    public Light(String id, String icon, String name, boolean enabled) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.enabled = enabled;
    }

    Light(LightGroup lightGroup) {
        this.id = lightGroup.macAddress;
        this.icon = lightGroup.icon;
        this.name = lightGroup.displayName;
        this.enabled = false;
    }

}
