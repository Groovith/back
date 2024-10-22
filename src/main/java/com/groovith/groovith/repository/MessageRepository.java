package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Message;
import com.groovith.groovith.domain.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long>, MessageRepositoryCustom {

    void deleteByUserChatRoom(UserChatRoom userChatRoom);
    void deleteByChatRoomId(Long chatRoomId);
//    List<Message> findAllByChatRoomIdOrderById(Long chatRoomId);

//
//    // 메세지 무한 스크롤 적용(마지막)
//    @Query("SELECT m FROM Message m " +
//            "WHERE m.id < :lastMessageId " +
//            "AND m.chatRoom.id = :chatRoomId " +
//            "ORDER BY m.id desc ")
//    Slice<Message> findMessagesV1(Long chatRoomId, Pageable pageable, Long lastMessageId);

}