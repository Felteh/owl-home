package com.owl.owlyhome.video;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class V1Play {
    public final String filename;
    public final String audio;

    @JsonCreator
    public V1Play(@JsonProperty("filename") String filename,@JsonProperty("audio") String audio) {
        this.filename = filename;
        this.audio = audio;
    }

    @Override
    public String toString() {
        return "V1Play{" + "filename=" + filename + ", audio=" + audio + '}';
    }
    
    
}
