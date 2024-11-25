package com.groovith.groovith.exception;

public class PlayerSessionNotFoundException extends RuntimeException {
    public PlayerSessionNotFoundException(Long chatRoomId) {
        super("No session found for chatRoomId: " + chatRoomId);
    }

    public PlayerSessionNotFoundException(String chatRoomId) {
        super("No session found for chatRoomId: " + chatRoomId);
    }
}
