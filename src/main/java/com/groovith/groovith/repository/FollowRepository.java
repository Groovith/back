package com.groovith.groovith.repository;

import com.groovith.groovith.domain.FollowEntity;
import com.groovith.groovith.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);
    void deleteByFollowerAndFollowing(UserEntity follower, UserEntity following);
}
