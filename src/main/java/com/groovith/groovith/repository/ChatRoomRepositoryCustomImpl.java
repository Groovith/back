package com.groovith.groovith.repository;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomStatus;
import com.groovith.groovith.domain.SearchChatRoomCategory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.groovith.groovith.domain.QChatRoom.chatRoom;

@RequiredArgsConstructor
@Repository
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;

    // 채팅방 검색
    @Override
    public Slice<ChatRoom> searchChatRoom(String query, Pageable pageable, Long lastChatRoomId) {
        List<ChatRoom> chatRooms = jpaQueryFactory
                .selectFrom(chatRoom)
                .where(searchChatRoomCondition(query, lastChatRoomId))
                .limit(pageable.getPageSize())
                .fetch();
        return new SliceImpl<>(chatRooms);
    }

    // 검색 조건
    private BooleanExpression searchChatRoomCondition(String query, Long lastChatRoomId){
        return chatRoom.name.contains(query)
                .and(isLastChatRoomId(lastChatRoomId)) // 검색된 채팅방 중 마지막 채팅방 id 이후로, 첫페이지일 경우 null
                .and(chatRoom.status.eq(ChatRoomStatus.PUBLIC));    // 공개 채팅방만
    }

    // 첫 페이지 일경우(lastChatRoomId == null) 검색 조건 무시
    private BooleanExpression isLastChatRoomId(Long lastChatRoomId){
        System.out.println("lastChatRoomId : "+lastChatRoomId);
        if(lastChatRoomId == null){
            return null;
        }
        return chatRoom.id.gt(lastChatRoomId);
    }

    //

}
