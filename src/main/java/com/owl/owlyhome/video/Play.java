package com.owl.owlyhome.video;

import com.owl.owlyhome.AudioOption;

public class Play {
    public final String filename;
    public final AudioOption audio;

    public Play(String filename, AudioOption audio) {
        this.filename = filename;
        this.audio = audio;
    }

    @Override
    public String toString() {
        return "Play{" + "filename=" + filename + ", audio=" + audio + '}';
    }
    
    
}
