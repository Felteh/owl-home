package com.owl.owlyhome.light;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class V1LightRequest {

    public String lightAddress;
    public String state;

    @JsonCreator
    public V1LightRequest(@JsonProperty("filename") String lightAddress, @JsonProperty("audio") String state) {
        this.lightAddress = lightAddress;
        this.state = state;
    }

    @Override
    public String toString() {
        return "V1LightRequest{" + "lightAddress=" + lightAddress + ", state=" + state + '}';
    }
}
