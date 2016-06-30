package com.example.hellosdl;

public class RadioStation {
    private String source;
    private String displayName;
    private String image;

    public RadioStation(String displayName, String source) {
        this.source = source;
        this.displayName = displayName;
    }

    public RadioStation(String source, String displayName, String image) {
        this.source = source;
        this.displayName = displayName;
        this.image = image;
    }

    public String getSource() {
        return source;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImage() {
        return image;
    }
}
