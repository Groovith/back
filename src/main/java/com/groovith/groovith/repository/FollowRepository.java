package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Follow;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(User follower, User following);
    void deleteByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
