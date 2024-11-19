package com.groovith.groovith.dto;

import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.enums.UserRelationship;
import lombok.Data;

@Data
public class ChatRoomMemberDto {

    private Long id;

    private String username;

    private String role;

    private String imageUrl;

    private UserRelationship userRelationship;

    public ChatRoomMemberDto(User user, UserRelationship userRelationship) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.imageUrl = user.getImageUrl();
        this.userRelationship = userRelationship;
    }
}
