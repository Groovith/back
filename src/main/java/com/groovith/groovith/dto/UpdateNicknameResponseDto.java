package com.groovith.groovith.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class UpdateNicknameResponseDto extends ResponseDto {
    private UpdateNicknameResponseDto() {
        super();
    }

    public static ResponseEntity<UpdateNicknameResponseDto> success() {
        UpdateNicknameResponseDto responseDto = new UpdateNicknameResponseDto();
        return ResponseEntity.ok(responseDto);
    }
}
