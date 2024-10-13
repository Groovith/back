package com.groovith.groovith.domain;

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

//    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE)
//    private final List<Message> messages = new ArrayList<>();

    // 현재 방에 있는 사람 수
    @Column(name = "current_member_count")
    private Integer currentMemberCount;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ChatRoomStatus status;

//    @Enumerated(EnumType.STRING)
//    private ChatRoomType type;

    @Column(name = "master_user_id")
    private Long masterUserId;

    @Builder
    public ChatRoom(String name, ChatRoomStatus chatRoomStatus) {
        this.name = name;
        this.status = chatRoomStatus;
        //this.type = chatRoomType;
        this.currentMemberCount = 1; // 채팅방이 생성될때 처음인원 1명
        this.imageUrl = "https://groovith-bucket.s3.ap-northeast-2.amazonaws.com/chatroom/chatroom_default.png";
    }

    /**
     * 비즈니스 메서드
     **/

    //채팅방에 유저 입장
    public void addUser(){
        this.currentMemberCount += 1;
    }

    //채팅방 유저 퇴장
    public void subUser(){
        this.currentMemberCount -= 1;
    }

    // 채팅방 이미지 변경
    public void setImageUrl(String url){
        this.imageUrl = url;
    }

    public void setMasterUserId(Long id){
        this.masterUserId = id;
    }
}