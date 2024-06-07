package com.groovith.groovith.domain.follow.dto;

import com.groovith.groovith.domain.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponse {
    private List<UserResponse> following;
    private List<UserResponse> follower;
}
