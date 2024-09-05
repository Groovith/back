package com.groovith.groovith.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class CheckUsernameResponseDto extends ResponseDto {
    private CheckUsernameResponseDto() {
        super();
    }

    public static ResponseEntity<CheckUsernameResponseDto> success() {
        CheckUsernameResponseDto responseDto = new CheckUsernameResponseDto();
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<ResponseDto> duplicateId() {
        ResponseDto responseDto = new ResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }
}
