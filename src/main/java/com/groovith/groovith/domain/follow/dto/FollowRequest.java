package com.groovith.groovith.domain.follow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequest {
    private String follower;
    private String following;
}
