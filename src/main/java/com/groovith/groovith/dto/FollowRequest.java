package com.groovith.groovith.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequest {
    private String follower;
    private String following;
}
