package com.groovith.groovith.dto;

import com.groovith.groovith.domain.Follow;
import com.groovith.groovith.domain.StreamingType;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.UserChatRoom;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
