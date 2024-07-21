package com.groovith.groovith.repository;

import com.groovith.groovith.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.userChatRoom WHERE u.id = :userId")
    UserEntity findByIdWithUserChatRoom(@Param("userId") Long userId);
}
