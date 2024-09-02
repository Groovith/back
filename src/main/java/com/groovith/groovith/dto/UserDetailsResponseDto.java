package com.groovith.groovith.dto;

import com.groovith.groovith.domain.FollowStatus;
import com.groovith.groovith.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponseDto {
    private Long id;
    private String username;
    private FollowStatus status;    // NOFOLLOW | ACCEPTED | PENDING | REJECTED
    private String imageUrl;
    private Integer following;
    private Integer follower;


    public UserDetailsResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.status = FollowStatus.NOFOLLOW;   // 기본 상태는 팔로우 x
        this.imageUrl = user.getImageUrl();
        this.follower = user.getFollowers().size();
        this.following = user.getFollowing().size();
    }
}
