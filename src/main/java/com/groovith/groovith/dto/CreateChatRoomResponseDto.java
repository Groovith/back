package com.groovith.groovith.dto;

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
}
