package com.groovith.groovith.domain;

import com.groovith.groovith.domain.enums.UserChatRoomStatus;
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

    // 채팅방이 삭제될때 메시지들도 같이 삭제 / 유저가 삭제될때는 메시지 유지
    @OneToMany(mappedBy = "userChatRoom", cascade = CascadeType.REMOVE)
    private final List<Message> messages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    // 현재 채팅방에 속해있는지
    private UserChatRoomStatus status;

    // 빌더패턴 + 양방향
    @Builder
    public UserChatRoom(User user, ChatRoom chatRoom, UserChatRoomStatus status){
        this.user = user;
        this.chatRoom = chatRoom;
        this.status = status;
    }

    /** 연관관계 편의 매서드 - UserChatRoom 에서 User, ChatRoom 양쪽 관리 **/


    public static UserChatRoom setUserChatRoom(User user, ChatRoom chatRoom,  UserChatRoomStatus status){
        UserChatRoom userChatRoom = UserChatRoom.builder()
                .user(user)
                .chatRoom(chatRoom)
                .status(status)
                .build();
        user.getUserChatRoom().add(userChatRoom);
        chatRoom.getUserChatRooms().add(userChatRoom);
        return userChatRoom;
    }

    public static void deleteUserChatRoom(UserChatRoom userChatRoom, User user, ChatRoom chatRoom){
        user.getUserChatRoom().remove(userChatRoom);
        chatRoom.getUserChatRooms().remove(userChatRoom);
    }

    public void setStatus(UserChatRoomStatus status){
        this.status = status;
    }
}
