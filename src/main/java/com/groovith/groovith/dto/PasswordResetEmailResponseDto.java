package com.groovith.groovith.dto;

import org.springframework.http.ResponseEntity;

public class PasswordResetEmailResponseDto extends ResponseDto{
    private PasswordResetEmailResponseDto() {
        super();
    }

    public ResponseEntity<PasswordResetEmailResponseDto> success() {
        PasswordResetEmailResponseDto responseDto = new PasswordResetEmailResponseDto();
        return ResponseEntity.ok(responseDto);
    }
}
