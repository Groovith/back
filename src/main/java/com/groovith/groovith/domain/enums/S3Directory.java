package com.groovith.groovith.domain.enums;

public enum S3Directory {
    USER("user/"),
    CHATROOM("chatroom/");

    private final String directory;
    private String defaultImageUrl;

    S3Directory(String directory) {
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    public String getDefaultImageUrl() {
        return defaultImageUrl;
    }

    public boolean isDefaultImage(String imageUrl) {
            return imageUrl.equals(defaultImageUrl);
    }

    public void setDefaultImageUrl(String defaultImageUrl) {
        this.defaultImageUrl = defaultImageUrl;
    }
}
