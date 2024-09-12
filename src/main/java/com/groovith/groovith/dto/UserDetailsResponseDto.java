package com.groovith.groovith.dto;

import com.groovith.groovith.domain.Follow;
import com.groovith.groovith.domain.FollowStatus;
import com.groovith.groovith.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponseDto {
    private Long id;
    private String username;
    private FollowStatus status;    // NOFOLLOW | ACCEPTED | PENDING | REJECTED
    private String imageUrl;
    private Integer followingCount;
    private Integer followerCount;


    public UserDetailsResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.status = FollowStatus.NOFOLLOW;   // 기본 상태는 팔로우 x, 검색 결과에는 포함x
        this.imageUrl = user.getImageUrl();
        // 승인된 팔로우만 카운트
        this.followingCount = user.getFollowing().stream().filter(f->f.getStatus()==FollowStatus.ACCEPTED).toList().size();
        this.followerCount = user.getFollowers().stream().filter(f->f.getStatus()==FollowStatus.ACCEPTED).toList().size();
    }
}
