package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.FollowStatus;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.enums.UserRelationship;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private UserRelationship userRelationship;

    public UserDetailsResponseDto(User user, UserRelationship userRelationship) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.status = FollowStatus.NOFOLLOW;   // 기본 상태는 팔로우 x, 검색 결과에는 포함x
        this.imageUrl = user.getImageUrl();
        // 승인된 팔로우만 카운트
        this.followingCount = user.getFollowing().stream().filter(f->f.getStatus()==FollowStatus.ACCEPTED).toList().size();
        this.followerCount = user.getFollowers().stream().filter(f->f.getStatus()==FollowStatus.ACCEPTED).toList().size();
        this.userRelationship = userRelationship;
    }
}
