package com.groovith.groovith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
public class EmailCertificationResponseDto {
    private String code;
    private String message;

    public static ResponseEntity<EmailCertificationResponseDto> success() {
        EmailCertificationResponseDto responseDto = new EmailCertificationResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<EmailCertificationResponseDto> duplicateId() {
        EmailCertificationResponseDto responseDto = new EmailCertificationResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    public static ResponseEntity<EmailCertificationResponseDto> mailSendFail() {
        EmailCertificationResponseDto responseDto = new EmailCertificationResponseDto(ResponseCode.MAIL_FAIL, ResponseMessage.MAIL_FAIL);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
    }
}
