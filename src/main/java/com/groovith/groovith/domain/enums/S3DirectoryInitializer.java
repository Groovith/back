package com.groovith.groovith.domain.enums;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class S3DirectoryInitializer {

    @Value("${cloud.aws.s3.defaultUserImageUrl}")
    private String defaultUserImageUrl;

    @Value("${cloud.aws.s3.defaultChatRoomImageUrl}")
    private String defaultChatRoomImageUrl;

    @PostConstruct
    public void init() {
        S3Directory.USER.setDefaultImageUrl(defaultUserImageUrl);
        S3Directory.CHATROOM.setDefaultImageUrl(defaultChatRoomImageUrl);
    }
}