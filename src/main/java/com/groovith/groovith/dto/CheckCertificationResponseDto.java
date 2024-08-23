package com.groovith.groovith.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor
public class CheckCertificationResponseDto {
    private String code;
    private String message;

    public static ResponseEntity<CheckCertificationResponseDto> success() {
        CheckCertificationResponseDto responseDto = new CheckCertificationResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<CheckCertificationResponseDto> certificationFail() {
        CheckCertificationResponseDto responseDto = new CheckCertificationResponseDto(ResponseCode.CERTIFICATION_FAIL, ResponseMessage.CERTIFICATION_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
    }

    public static ResponseEntity<CheckCertificationResponseDto> databaseError() {
        CheckCertificationResponseDto responseDto = new CheckCertificationResponseDto(ResponseCode.DATABASE_ERROR, ResponseMessage.DATABASE_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
    }
}
