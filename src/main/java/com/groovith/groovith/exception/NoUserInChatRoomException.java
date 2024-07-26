package com.groovith.groovith.exception;

public class NoUserInChatRoomException extends RuntimeException{
    public NoUserInChatRoomException(Long chatRoomId) {
        super("No user in chatRoom id:"+chatRoomId+" chatroom is already empty");
    }


}
