package com.groovith.groovith.dto;


import com.groovith.groovith.domain.ChatRoom;
import lombok.Data;

import java.util.Date;

/**
 *  채팅창 목록 조회시 response dto
 *  {
 * 		chatRoomId: 1
 * 		chatRoomName: "room1"
 * 		totalMember: 4
 * 		currentMember: 3
 * 		currentSong: "song1"
 * 		lastMessage: "hello"
 * 		lastSentAt: 12:40:49
 * 	    },
 * */
@Data
public class ChatRoomListResponseDto {

    private Long chatRoomId;
    private String chatRoomName;
    private int totalMember;
    private int currentMember;
    private String currentSong;
    private  String lastMessage;
    private Date lastSentAt;

    public ChatRoomListResponseDto(ChatRoom chatRoom){
        this.chatRoomId = chatRoom.getId();
        this.chatRoomName = chatRoom.getName();
        this.totalMember = chatRoom.getTotalMember();
        this.currentMember = chatRoom.getCurrentMember();
    }
//    @Builder
//    public ChatRoomListResponseDto(Long chatRoomId, String chatRoomName, int totalMember,
//                                   int currentMember, String currentSong
//                                    ,String lastMessage, Date lastSentAt)
//    {
//        this.chatRoomId = chatRoomId;
//        this.chatRoomName = chatRoomName;
//        this.totalMember = totalMember;
//        this.currentMember = currentMember;
//        this.currentSong = currentSong;
//        this.lastMessage = lastMessage;
//        this.lastSentAt = lastSentAt;
//    }
}
