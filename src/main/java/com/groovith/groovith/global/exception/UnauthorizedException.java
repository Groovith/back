package com.groovith.groovith.global.exception;

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
