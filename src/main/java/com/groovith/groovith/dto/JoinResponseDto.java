package com.groovith.groovith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
public class JoinResponseDto {
    private String code;
    private String message;

    // note: static 키워드가 붙은 메서드는 클래스 자체에 속하며, 객체를 생성하지 않고도 클래스명으로 직접 호출할 수 있다.
    public static ResponseEntity<JoinResponseDto> success() {
        JoinResponseDto responseDto = new JoinResponseDto(ResponseCode.SUCCESS, ResponseMessage.SUCCESS);
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<JoinResponseDto> certificationFail() {
        JoinResponseDto responseDto = new JoinResponseDto(ResponseCode.CERTIFICATION_FAIL, ResponseMessage.CERTIFICATION_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
    }

    public static ResponseEntity<JoinResponseDto> duplicateId() {
        JoinResponseDto responseDto = new JoinResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    public static ResponseEntity<JoinResponseDto> databaseError() {
        JoinResponseDto responseDto = new JoinResponseDto(ResponseCode.DATABASE_ERROR, ResponseMessage.DATABASE_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
    }
}
