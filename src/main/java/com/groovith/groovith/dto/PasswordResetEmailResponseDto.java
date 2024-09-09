package com.groovith.groovith.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PasswordResetEmailResponseDto extends ResponseDto{
    private PasswordResetEmailResponseDto() {
        super();
    }

    public static ResponseEntity<PasswordResetEmailResponseDto> success() {
        PasswordResetEmailResponseDto responseDto = new PasswordResetEmailResponseDto();
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<ResponseDto> mailSendFail() {
        ResponseDto responseDto = new ResponseDto(ResponseCode.MAIL_FAIL, ResponseMessage.MAIL_FAIL);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
    }
}
