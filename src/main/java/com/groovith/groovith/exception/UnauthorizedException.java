package com.groovith.groovith.exception;

import org.springframework.web.client.HttpClientErrorException;

public class UnauthorizedException extends RuntimeException{


    public UnauthorizedException(){
        super();
    }

    public UnauthorizedException(String message){
        super(message);
    }


    public UnauthorizedException(String message, Throwable cause){
        super(message, cause);
    }
}
