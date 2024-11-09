package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.StreamingType;
import com.groovith.groovith.domain.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class UserChatRoomDto {

    private Long id;

    private String username;

    private String role;

    @Enumerated(EnumType.STRING)
    private StreamingType streaming;

    public UserChatRoomDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.streaming = user.getStreaming();
    }
}
