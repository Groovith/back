package com.groovith.groovith.repository;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.UserChatRoom;
import com.groovith.groovith.domain.enums.UserChatRoomStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    Optional<UserChatRoom> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    List<UserChatRoom> findByUserId(Long id);

    @Query("SELECT ucr.chatRoom FROM UserChatRoom ucr " +
            "JOIN FETCH ucr.chatRoom cr " +
            "WHERE ucr.user.id = :userId " +
            "AND ucr.status = :status")
    List<ChatRoom> findEnterChatRoomsByUserId(@Param("userId") Long userId, @Param("status") UserChatRoomStatus status);


    @Query("SELECT ucr FROM UserChatRoom ucr " +
            "JOIN FETCH ucr.user u " +
            "WHERE ucr.chatRoom.id = :chatRoomId " +
            "AND ucr.status = :status")
    List<UserChatRoom> findEnterUserChatRoomsByChatRoomId(@Param("chatRoomId")Long chatRoomId, @Param("status") UserChatRoomStatus status);

}
