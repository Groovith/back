package com.groovith.groovith.dto;

import com.groovith.groovith.domain.User;
import lombok.Data;

@Data
public class ChatRoomMemberDto {

    private Long id;

    private String username;

    private String role;

    private String imageUrl;

    public ChatRoomMemberDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.imageUrl = user.getImageUrl();
    }
}
