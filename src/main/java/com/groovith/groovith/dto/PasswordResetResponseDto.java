package com.groovith.groovith.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PasswordResetResponseDto extends ResponseDto {
    private PasswordResetResponseDto() {
        super();
    }

    public static ResponseEntity<PasswordResetResponseDto> success() {
        return ResponseEntity.ok(new PasswordResetResponseDto());
    }

    public static ResponseEntity<ResponseDto> certificationFail() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseDto(ResponseCode.CERTIFICATION_FAIL, ResponseMessage.CERTIFICATION_FAIL));
    }
}
