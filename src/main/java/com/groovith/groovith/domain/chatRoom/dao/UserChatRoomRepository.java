package com.groovith.groovith.domain.chatRoom.dao;

import com.groovith.groovith.domain.chatRoom.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    UserChatRoom findByUserIdAndChatRoomId(Long userId, Long chatRoomId);
}
