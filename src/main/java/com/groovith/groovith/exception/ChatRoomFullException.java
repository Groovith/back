package com.groovith.groovith.exception;

public class ChatRoomFullException extends RuntimeException{
    public ChatRoomFullException(Long id) {
        super("ChatRoom with id: " + id + " is full.");
    }
}
