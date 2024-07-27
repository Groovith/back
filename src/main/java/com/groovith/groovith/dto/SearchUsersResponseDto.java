package com.groovith.groovith.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchUsersResponseDto {
    private final List<UserDetailsResponse> users;
}
