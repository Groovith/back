package com.groovith.groovith.dto;

import com.groovith.groovith.domain.UserChatRoom;
import com.groovith.groovith.domain.CommandType;
import com.groovith.groovith.domain.Message;
import com.groovith.groovith.domain.MessageType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통신에 사용할 메세지
 * */
@Data
public class MessageDto {

    private String content;

    private Long chatRoomId;

    private Long userId;

    private String username;

    private MessageType type;   //  CHAT, JOIN, LEAVE, PLAYER

    private CommandType command;

    private String track;

    public Message toEntity(UserChatRoom userChatRoom){
        return Message.builder()
                .content(content)
                .messageType(type)
                .userChatRoom(userChatRoom)
                .build();
    }
//
//    @Builder
//    public Message(String content, UserChatRoom userChatRoom, MessageType messageType){
//        this.content = content;
//        this.userChatRoom = userChatRoom;
//        this.messageType = messageType;
//        userChatRoom.getMessages().add(this);
//    }
//

}
