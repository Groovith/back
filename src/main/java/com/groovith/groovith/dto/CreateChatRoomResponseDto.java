package com.groovith.groovith.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  ChatRoom 생성시 response dto
 * */

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateChatRoomResponseDto  {
    private Long chatRoomId;

    @Builder
    public CreateChatRoomResponseDto(Long chatRoomId){
        this.chatRoomId = chatRoomId;
    }
}
