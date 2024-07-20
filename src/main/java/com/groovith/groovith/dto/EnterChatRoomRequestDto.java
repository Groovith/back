package com.groovith.groovith.dto;

import lombok.Data;

@Data
public class EnterChatRoomRequestDto {
    private Long userId;
    private Long chatRoomId;
}
