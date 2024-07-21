package com.groovith.groovith.dto;

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
