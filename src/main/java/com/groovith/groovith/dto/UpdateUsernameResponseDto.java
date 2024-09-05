package com.groovith.groovith.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class UpdateUsernameResponseDto extends ResponseDto {
    private UpdateUsernameResponseDto() {
        super();
    }

    public static ResponseEntity<UpdateUsernameResponseDto> success() {
        UpdateUsernameResponseDto responseDto = new UpdateUsernameResponseDto();
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<ResponseDto> duplicateId() {
        ResponseDto responseDto = new ResponseDto(ResponseCode.DUPLICATE_ID, ResponseMessage.DUPLICATE_ID);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }
}
