package com.groovith.groovith.repository;


import com.groovith.groovith.domain.Message;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.groovith.groovith.domain.QMessage.message;
@RequiredArgsConstructor
@Repository
public class MessageRepositoryCustomImpl implements MessageRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;
    private static final int MESSAGE_LIST_LIMIT_SIZE = 20;  // 한페이지에 불러올 메세지 양

    @Override
    public Slice<Message> findMessages(Long chatRoomId, Long lastMessageId) {

        List<Message> messages = jpaQueryFactory
                .selectFrom(message)
                .where(findCondition(chatRoomId, lastMessageId))
                .limit(MESSAGE_LIST_LIMIT_SIZE)
                .orderBy(message.id.desc())
                .fetch();

        return new SliceImpl<>(messages);
    }

    private BooleanExpression findCondition(Long chatRoomId, Long lastMessageId){
        return message.chatRoomId.eq(chatRoomId)
                .and(isLastMessageId(lastMessageId));
    }

    // lastMessageId 가 없을 때 조건
    private BooleanExpression isLastMessageId(Long lastMessageId){
        // 첫 페이지일 시에 가장 최근의 메세지부터 20개 불러오기
        if(lastMessageId == null){
            return null;
        }
        return message.id.lt(lastMessageId);
    }
}

