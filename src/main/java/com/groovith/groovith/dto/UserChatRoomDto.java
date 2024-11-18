package com.groovith.groovith.dto;

import com.groovith.groovith.domain.User;
import lombok.Data;

@Data
public class UserChatRoomDto {

    private Long id;

    private String username;

    private String role;

    public UserChatRoomDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
    }
}
