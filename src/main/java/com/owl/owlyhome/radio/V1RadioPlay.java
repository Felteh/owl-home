package com.owl.owlyhome.radio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class V1RadioPlay {

    public final String station;

    @JsonCreator
    public V1RadioPlay(@JsonProperty("station") String station) {
        this.station = station;
    }

    @Override
    public String toString() {
        return "V1RadioPlay{" + "station=" + station + '}';
    }

}
