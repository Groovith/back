package com.groovith.groovith.repository;

import com.groovith.groovith.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    Optional<UserChatRoom> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    List<UserChatRoom> findByUserId(Long id);

}
