package com.groovith.groovith.exception;

public class NoUserInChatRoomException extends RuntimeException{
    public NoUserInChatRoomException(Long chatRoomId) {
        super("chatRoom_id : " + chatRoomId + " no User. chatroom is empty");
    }


}
