package com.owl.owlyhome;

public enum AudioOption {
    HDMI("hdmi"),
    LOCAL("local"),
    BOTH("both");
    
    public final String commandLine;
    
    private AudioOption(String comandLine){
        this.commandLine=comandLine;
    }
    
    public static AudioOption fromRestApi(String i){
        for(AudioOption o : AudioOption.values()){
            if(i.equals(o.commandLine)){
                return o;
            }
        }
        throw new RuntimeException("Could not find relevant audio option");
    }
}
