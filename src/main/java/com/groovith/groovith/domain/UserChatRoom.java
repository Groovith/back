package com.groovith.groovith.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserChatRoom{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userChatRoom_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chatRoom_id")
    private ChatRoom chatRoom;

    @OneToMany(mappedBy = "userChatRoom")
    private List<Message> messages = new ArrayList<>();

    // 빌더패턴 + 양방향
    @Builder
    public UserChatRoom(User user, ChatRoom chatRoom){
        this.user = user;
        this.chatRoom = chatRoom;
    }

    /** 연관관계 편의 매서드 - UserChatRoom 에서 User, ChatRoom 양쪽 관리 **/


    public static UserChatRoom setUserChatRoom(User user, ChatRoom chatRoom){
        UserChatRoom userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();
        user.getUserChatRoom().add(userChatRoom);
        chatRoom.getUserChatRooms().add(userChatRoom);
        return userChatRoom;
    }

    public static void deleteUserChatRoom(UserChatRoom userChatRoom, User user, ChatRoom chatRoom){
        user.getUserChatRoom().remove(userChatRoom);
        chatRoom.getUserChatRooms().remove(userChatRoom);
    }


    /** 비즈니스 로직 **/

}
