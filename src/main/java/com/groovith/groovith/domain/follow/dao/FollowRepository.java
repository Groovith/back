package com.groovith.groovith.domain.follow.dao;

import com.groovith.groovith.domain.follow.domain.FollowEntity;
import com.groovith.groovith.domain.user.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);
    void deleteByFollowerAndFollowing(UserEntity follower, UserEntity following);
}
