package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepositoryCustom {
    Slice<Message> findMessages(Long chatRoomId, Long LastMessageId);
}
