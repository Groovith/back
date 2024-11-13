package com.groovith.groovith.exception;

public class UserAlreadyInChatRoomException extends RuntimeException {
    public UserAlreadyInChatRoomException(Long inviteeId, Long chatRoomId) {
        super("유저가 채팅방에 이미 참가중 " + " userId =" + inviteeId + " chatRoomId= " + chatRoomId);
    }
}
