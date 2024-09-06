package com.groovith.groovith.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DeleteAccountResponseDto extends ResponseDto{
    private DeleteAccountResponseDto() {
        super();
    }

    public static ResponseEntity<DeleteAccountResponseDto> success() {
        DeleteAccountResponseDto responseDto = new DeleteAccountResponseDto();
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<ResponseDto> wrongPassword() {
        ResponseDto responseDto = new ResponseDto(ResponseCode.WRONG_PASSWORD, ResponseMessage.WRONG_PASSWORD);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
    }
}
