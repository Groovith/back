package com.groovith.groovith.exception;

// 유저가 채팅방에 없음
public class UserChatRoomNotFoundException extends RuntimeException{
    public UserChatRoomNotFoundException() {super();}

    public UserChatRoomNotFoundException(Long userId, Long chatRoomId) {
        super("UserId :"+userId+" ChatRoomId :"+chatRoomId+" Not Found");
    }
}
