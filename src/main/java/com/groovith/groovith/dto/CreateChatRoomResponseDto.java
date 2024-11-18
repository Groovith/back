package com.groovith.groovith.dto;

import com.groovith.groovith.domain.ChatRoom;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  ChatRoom 생성시 response dto
 * */

@Data
public class CreateChatRoomResponseDto  {
    private Long chatRoomId;

    public CreateChatRoomResponseDto(ChatRoom chatRoom) {
        this.chatRoomId = chatRoom.getId();
    }
}
