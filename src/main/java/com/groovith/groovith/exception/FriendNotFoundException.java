package com.groovith.groovith.exception;

public class FriendNotFoundException extends RuntimeException {
    public FriendNotFoundException(String message) {
        super(message);
    }
    public FriendNotFoundException(Long fromUserId, Long toUserId) {
        super("친구 관계가 아닙니다. from: " + fromUserId + " to: " + toUserId);
    }
}
