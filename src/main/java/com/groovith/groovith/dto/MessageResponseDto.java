package com.groovith.groovith.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.groovith.groovith.domain.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageResponseDto {
    private Long messageId;
    private Long chatRoomId;
    private Long userId;
    private String username;
    private String content;
    private MessageType type;
    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @Builder
    public MessageResponseDto(Long messageId, Long chatRoomId, Long userId, String username, String content, MessageType type, LocalDateTime createdAt, String imageUrl) {
        this.messageId = messageId;
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }
}
