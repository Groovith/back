package com.groovith.groovith.repository;

import com.groovith.groovith.domain.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

//    List<User> findByUsernameContaining(String username);

    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u " +
            "WHERE u.username LIKE %:username% " +
            "AND u.id> :lastUserId")
    Slice<User> findByUsernameContaining(@Param("username") String username, Pageable pageable, @Param("lastUserId") Long lastUserId);
}
