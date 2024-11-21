package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.ChatRoomPrivacy;
import lombok.Getter;

@Getter
public class UpdateChatRoomRequestDto {
    private String name;
    private ChatRoomPrivacy status;
    private ChatRoomPermission permission;
    private String imageUrl;
}
