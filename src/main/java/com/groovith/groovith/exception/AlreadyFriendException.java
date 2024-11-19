package com.groovith.groovith.exception;

public class AlreadyFriendException extends RuntimeException {
    public AlreadyFriendException(String message) {
        super(message);
    }
    public AlreadyFriendException(Long fromUserId, Long toUserId) {
        super("이미 친구관계입니다. from:" + fromUserId + " to:" + toUserId);
    }
}
