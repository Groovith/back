package com.groovith.groovith.domain;

import com.groovith.groovith.domain.enums.MessageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name="userChatRoom_id")
    private UserChatRoom userChatRoom;

    @Column(name = "chatroom_id")
    private Long chatRoomId;

    private String imageUrl;


    @Builder
    public Message(String content, UserChatRoom userChatRoom, MessageType messageType, Long chatRoomId, String imageUrl){
        this.content = content;
        this.userChatRoom = userChatRoom;
        this.messageType = messageType;
        this.chatRoomId = chatRoomId;
        this.imageUrl = imageUrl;
    }

    /**
     * 연관관계 편의 메서드, 메시지 생성은 setMessage()로 생성
     * */
    public static Message setMessage(String content, MessageType messageType, UserChatRoom userChatRoom, Long chatRoomId, String imageUrl){
        Message message = Message.builder()
                .content(content)
                .messageType(messageType)
                .userChatRoom(userChatRoom)
                .chatRoomId(chatRoomId)
                .imageUrl(imageUrl)
                .build();
        userChatRoom.getMessages().add(message);
        return message;
    }

    /**
     * 비즈니스 메서드
     * */
    // 유저가 탈퇴될때 메시지 연관관계 삭제
    public void setUserChatRoomNull(){
        this.userChatRoom = null;
    }
}
