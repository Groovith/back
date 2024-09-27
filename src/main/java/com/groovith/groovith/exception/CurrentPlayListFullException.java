package com.groovith.groovith.exception;

public class CurrentPlayListFullException extends RuntimeException{
    public CurrentPlayListFullException(String id) {
        super("PlayList with id: " + id + " is full.");
    }

    public CurrentPlayListFullException(Long id) {
        super("PlayList with id: " + id + " is full.");
    }

}
