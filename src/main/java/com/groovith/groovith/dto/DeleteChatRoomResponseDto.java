package com.groovith.groovith.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class DeleteChatRoomResponseDto extends ResponseDto {
    private DeleteChatRoomResponseDto(){
        super();
    }

    public static ResponseEntity<DeleteChatRoomResponseDto> success() {
        DeleteChatRoomResponseDto responseDto = new DeleteChatRoomResponseDto();
        return ResponseEntity.ok(responseDto);
    }

    public static ResponseEntity<ResponseDto> notMasterUser() {
        ResponseDto responseDto = new ResponseDto(ResponseCode.NOT_MASTER_USER, ResponseMessage.NOT_MASTER_USER);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
    }
}
