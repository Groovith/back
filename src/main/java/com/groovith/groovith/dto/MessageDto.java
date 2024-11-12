package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.MessageType;
import lombok.Builder;
import lombok.Data;

/**
 *
 * */
@Data
public class MessageDto {

    private String content;

    private Long chatRoomId;

    private Long userId;

    private String username;

    private MessageType type;   //  CHAT, JOIN, LEAVE, PLAYER

    private String imageUrl;

    @Builder
    public MessageDto(String content, Long chatRoomId, Long userId, String username, MessageType type, String imageUrl){
        this.content = content;
        this.chatRoomId = chatRoomId;
        this.userId = userId;
        this.username = username;
        this.type = type;
        this.imageUrl = imageUrl;
    }
}
