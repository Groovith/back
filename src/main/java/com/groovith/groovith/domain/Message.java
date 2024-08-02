package com.groovith.groovith.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Message extends BaseTime{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chatRoom_id")
    private ChatRoom chatRoom;

    @Column(name = "user_id")
    private Long userId;

    @Builder
    public Message(String content,ChatRoom chatRoom, Long userId, MessageType messageType){
        this.content = content;
        this.messageType = messageType;
        this.chatRoom = chatRoom;
        this.userId = userId;
    }

    /**
     * 연관관계 편의 메서드, 메세지 생성은 setMessage()로 생성
     * */
    public static Message setMessage(String content,ChatRoom chatRoom, Long userId, MessageType messageType){
        Message message = Message.builder()
                .content(content)
                .messageType(messageType)
                .chatRoom(chatRoom)
                .userId(userId)
                .build();
        chatRoom.getMessages().add(message);
        return message;
    }
}
