package com.groovith.groovith.exception;

public class NotMasterUserException extends RuntimeException {
    public NotMasterUserException(String message) {
        super(message);
    }
}
