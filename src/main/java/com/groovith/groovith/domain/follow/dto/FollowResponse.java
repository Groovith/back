package com.groovith.groovith.domain.follow.dto;

import com.groovith.groovith.domain.user.dto.UserDetailsResponse;
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
    private List<UserDetailsResponse> following;
    private List<UserDetailsResponse> follower;
}
