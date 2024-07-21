package com.groovith.groovith.repository;

import com.groovith.groovith.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u JOIN FETCH u.userChatRoom WHERE u.id = :userId")
    User findByIdWithUserChatRoom(@Param("userId") Long userId);
}
