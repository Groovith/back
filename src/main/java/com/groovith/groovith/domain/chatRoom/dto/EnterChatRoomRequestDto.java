package com.groovith.groovith.domain.chatRoom.dto;

import lombok.Data;

@Data
public class EnterChatRoomRequestDto {
    private Long userId;
    private Long chatRoomId;
}
