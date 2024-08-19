package com.groovith.groovith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailCheckResponseDto {
    private String code;
    private String message;

    public static ResponseEntity<EmailCheckResponseDto> success() {
        EmailCheckResponseDto responseDto = new EmailCheckResponseDto(ResponseCode.SUCCESS, "Success.");
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<EmailCheckResponseDto> duplicateId() {
        EmailCheckResponseDto responseDto = new EmailCheckResponseDto(ResponseCode.DUPLICATE_ID, "Duplicate Id.");
        return ResponseEntity.badRequest().body(responseDto);
    }

    public static ResponseEntity<EmailCheckResponseDto> databaseError() {
        EmailCheckResponseDto responseDto = new EmailCheckResponseDto(ResponseCode.DATABASE_ERROR, "Database error.");
        return ResponseEntity.internalServerError().body(responseDto);
    }
}
