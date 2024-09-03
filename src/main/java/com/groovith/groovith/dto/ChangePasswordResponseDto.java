package com.groovith.groovith.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ChangePasswordResponseDto extends ResponseDto{
    private ChangePasswordResponseDto() {
        super();
    }

    public static ResponseEntity<ChangePasswordResponseDto> success() {
        ChangePasswordResponseDto responseDto = new ChangePasswordResponseDto();
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<ResponseDto> wrongPassword() {
        ResponseDto responseDto = new ResponseDto(ResponseCode.WRONG_PASSWORD, ResponseMessage.WRONG_PASSWORD);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
    }
}
