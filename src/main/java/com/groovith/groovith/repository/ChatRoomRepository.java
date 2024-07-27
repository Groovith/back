package com.groovith.groovith.repository;

import com.groovith.groovith.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("Select c FROM ChatRoom c ORDER BY c.id DESC ")
    List<ChatRoom> findAllDesc();

    List<ChatRoom> findChatRoomByNameContaining(String name);
}
