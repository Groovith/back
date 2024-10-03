package com.groovith.groovith.repository;

import com.groovith.groovith.domain.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChatRoomRepositoryCustom {

    Slice<ChatRoom> searchChatRoom(String query, Pageable pageable, Long lastChatRoomId);
}
