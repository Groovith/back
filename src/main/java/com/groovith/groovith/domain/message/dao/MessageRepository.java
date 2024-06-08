package com.groovith.groovith.domain.message.dao;

import com.groovith.groovith.domain.message.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByChatRoomId(Long chatRoomId);

    //List<Message> findAllByChatRoomIdOrderById(Long chatRoomId);
}