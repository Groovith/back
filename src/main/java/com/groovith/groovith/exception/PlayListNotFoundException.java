package com.groovith.groovith.exception;

public class PlayListNotFoundException extends RuntimeException {
    public PlayListNotFoundException(Long chatRoomId) {
        super("No playList found for chatRoomId: " + chatRoomId);
    }
}
