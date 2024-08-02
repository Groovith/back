package com.groovith.groovith.dto;

import com.groovith.groovith.domain.MessageType;
import lombok.Data;

@Data
public class MessageResponseDto {
    private Long messageId;
    private Long chatRoomId;
    private Long userId;
    private String username;
    private String content;
    private MessageType type;
}
