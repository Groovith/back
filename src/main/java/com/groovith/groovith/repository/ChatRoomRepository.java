package com.groovith.groovith.repository;

import com.groovith.groovith.domain.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom {
    @Query("Select c FROM ChatRoom c ORDER BY c.id DESC ")
    List<ChatRoom> findAllDesc();

//    List<ChatRoom> findChatRoomByMasterUserName(String masterUserName);

//    @Query("SELECT c FROM ChatRoom c " +
//            "WHERE c.name LIKE %:name% " +
//            "AND c.id> :lastChatRoomId"
//    )
//    Slice<ChatRoom> findChatRoomByNameContaining(
//            @Param("name") String name,
//            Pageable pageable,
//            @Param("lastChatRoomId") Long lastChatRoomId
//    );

}
