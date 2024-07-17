package com.groovith.groovith.domain.message.domain;

import com.groovith.groovith.domain.chatRoom.domain.UserChatRoom;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Message{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userChatRoom_id")
    UserChatRoom userChatRoom;

    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(name = "chatRoom_id")
    private Long chatRoomId;

    @Column(name = "user_id")
    private Long userId;

    @Builder
    public Message(String content, UserChatRoom userChatRoom, MessageType messageType){
        this.content = content;
        this.userChatRoom = userChatRoom;
        this.messageType = messageType;
        this.chatRoomId = userChatRoom.getChatRoom().getId();
        this.userId = userChatRoom.getUser().getId();
        userChatRoom.getMessages().add(this);
    }
}
