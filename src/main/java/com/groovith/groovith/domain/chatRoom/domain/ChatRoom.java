package com.groovith.groovith.domain.chatRoom.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 *  채팅방 엔티티
 *
 * */

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatRoom_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private final List<UserChatRoom> userChatRooms = new ArrayList<>();

    private String imageUrl;

    // 현재 방에 있는 사람 수
    private int currentMember;

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;


    @Builder
    public ChatRoom(String name, ChatRoomType chatRoomType) {
        this.name = name;
        this.type = chatRoomType;
        this.currentMember = 1; // 생성될때 멤버 1명
    }
    /**
     * 비즈니스 메서드
     **/
    // 수정 필요
    public int getTotalMember(){
        return this.getUserChatRooms().size();
    }

    // 채팅방의 제일 먼저 들어온 유저 아이디 가져가기(아무거나 가져가도 상관은없음. 일단은 제일앞에거)
    public Long getMasterId(){
        List<UserChatRoom> userChatRoom = this.getUserChatRooms();
        if (userChatRoom.isEmpty()){
            throw new NullPointerException();
        }
        else{
            return userChatRoom.get(0).getUser().getId();
        }
    }

    // 채팅방에 유저 입장
    public void addUser(){
        this.currentMember += 1;
    }

    // 채팅방 유저 퇴장
    public void subUser(){
        this.currentMember -= 1;
    }

}


