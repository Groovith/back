package com.groovith.groovith.exception;

public class NotMasterUserException extends RuntimeException {
    public NotMasterUserException(String message) {
        super(message);
    }

    public NotMasterUserException(Long userId) {
        super("user id " + userId + " is not master user");
    }
}
