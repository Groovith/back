package com.groovith.groovith.exception;

public class ChatRoomNotFoundException extends RuntimeException{
    public ChatRoomNotFoundException(Long id) {
        super("ChatRoom with id: " + id + " not found.");
    }
}
