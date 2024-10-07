package com.groovith.groovith.repository;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long>, MessageRepositoryCustom {
    List<Message> findAllByChatRoomId(Long chatRoomId);

    //List<Message> findAllByChatRoomIdOrderById(Long chatRoomId);
    @Query("SELECT c FROM ChatRoom c " +
            "WHERE c.name LIKE %:name% " +
            "AND c.id> :lastChatRoomId"
    )
    Slice<ChatRoom> findChatRoomByNameContaining(
            @Param("name") String name,
            Pageable pageable,
            @Param("lastChatRoomId") Long lastChatRoomId
    );

    // 메세지 무한 스크롤 적용(마지막)
    @Query("SELECT m FROM Message m " +
            "WHERE m.id < :lastMessageId " +
            "AND m.chatRoom.id = :chatRoomId " +
            "ORDER BY m.id desc ")
    Slice<Message> findMessagesV1(Long chatRoomId, Pageable pageable, Long lastMessageId);

}