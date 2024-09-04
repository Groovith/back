package com.groovith.groovith.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class UpdatePasswordResponseDto extends ResponseDto{
    private UpdatePasswordResponseDto() {
        super();
    }

    public static ResponseEntity<UpdatePasswordResponseDto> success() {
        UpdatePasswordResponseDto responseDto = new UpdatePasswordResponseDto();
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<ResponseDto> wrongPassword() {
        ResponseDto responseDto = new ResponseDto(ResponseCode.WRONG_PASSWORD, ResponseMessage.WRONG_PASSWORD);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
    }
}
