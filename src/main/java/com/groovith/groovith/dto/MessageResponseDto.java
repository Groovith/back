package com.groovith.groovith.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.groovith.groovith.domain.enums.MessageType;
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
}
