package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.StreamingType;
import com.groovith.groovith.domain.User;
import lombok.Data;

@Data
public class CurrentUserDetailsDto {
    private Long id;
    private String username;
    private StreamingType streaming;
    private String imageUrl;

    public CurrentUserDetailsDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.streaming = user.getStreaming();
        this.imageUrl = user.getImageUrl();
    }
}
