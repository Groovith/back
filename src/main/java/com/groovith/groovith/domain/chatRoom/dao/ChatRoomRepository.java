package com.groovith.groovith.domain.chatRoom.dao;

import com.groovith.groovith.domain.chatRoom.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("Select c FROM ChatRoom c ORDER BY c.id DESC ")
    List<ChatRoom> findAllDesc();
}
