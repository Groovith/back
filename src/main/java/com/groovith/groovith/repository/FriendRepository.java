package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Friend;
import com.groovith.groovith.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    Optional<Friend> findByFromUserAndToUser(User fromUser, User toUser);

    // user 의 friend 중 from_user 가 user 인 to_user id들
    @Query("select f.toUser.id from Friend f "
            +"where f.fromUser = :user ")
    List<Long> findFriendsIdsFromUser(@Param("user") User user);
}
