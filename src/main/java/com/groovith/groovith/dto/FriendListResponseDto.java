package com.groovith.groovith.dto;

import lombok.Data;

import java.util.List;

@Data
public class FriendListResponseDto {
    private final List<UserDetailsResponseDto> friends;
}
