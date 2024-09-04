package com.groovith.groovith.dto;

import org.springframework.http.ResponseEntity;

public class DeleteProfilePictureResponseDto extends ResponseDto{
    private DeleteProfilePictureResponseDto() {
        super();
    }

    public static ResponseEntity<DeleteProfilePictureResponseDto> success() {
        DeleteProfilePictureResponseDto responseDto = new DeleteProfilePictureResponseDto();
        return ResponseEntity.ok(responseDto);
    }
}
