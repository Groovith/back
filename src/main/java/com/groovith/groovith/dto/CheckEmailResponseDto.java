package com.groovith.groovith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckEmailResponseDto {
    private String code;
    private String message;

    public static ResponseEntity<CheckEmailResponseDto> success() {
        CheckEmailResponseDto responseDto = new CheckEmailResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<CheckEmailResponseDto> duplicateId() {
        CheckEmailResponseDto responseDto = new CheckEmailResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.badRequest().body(responseDto);
    }

    public static ResponseEntity<CheckEmailResponseDto> databaseError() {
        CheckEmailResponseDto responseDto = new CheckEmailResponseDto(ResponseCode.DATABASE_ERROR, ResponseMessage.DATABASE_ERROR);
        return ResponseEntity.internalServerError().body(responseDto);
    }
}
