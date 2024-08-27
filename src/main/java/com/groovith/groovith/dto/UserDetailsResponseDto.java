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
    boolean isFollowing;    // 현재 로그인 중인 유저가 팔로우 중인 유저인지

    public UserDetailsResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.isFollowing = false;   // 기본 상태는 팔로우 x
    }
}
