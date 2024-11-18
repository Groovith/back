package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.ChatRoomStatus;
import lombok.Getter;

@Getter
public class UpdateChatRoomRequestDto {
    private String name;
    private ChatRoomStatus status;
    private ChatRoomPermission permission;
}
