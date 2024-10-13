package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Friend;
import com.groovith.groovith.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    Optional<Friend> findByFromUserAndToUser(User fromUser, User toUser);
}
