package com.groovith.groovith.handler;

import com.groovith.groovith.dto.ResponseDto;
import com.groovith.groovith.exception.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserNotFoundExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ResponseDto> userNotFoundExceptionHandler() {
        return ResponseDto.noSuchUser();
    }
}
