package com.groovith.groovith.dto;


import com.groovith.groovith.domain.ChatRoom;
import lombok.Data;

/**
 *  채팅방 정보 조회
 *  chatRoomId: "",
 *  name: "",
 *  image: {
 * 	      url: "",
 * 	      width: 300,
 * 	      height: 300,
 *       },
 *  totalUsers: 0,
 *  currentUsers: 0,
 *  masterId: "",
 *  playlistId: "",
 *  playlistIndex: 0,
 *  postion: 0,
 *  paused: true | false,
 **/

@Data
public class ChatRoomDetailDto {

    private Long chatRoomId;
    private String name;
    private int totalUsers;
    private int currentUsers;
    private Long masterId;
    private Long playListId;
    private int playListIndex;
    private int position;
    private Boolean paused;

    // position paused

    public ChatRoomDetailDto(ChatRoom chatRoom){
        this.chatRoomId = chatRoom.getId();
        this.name = chatRoom.getName();
        this.totalUsers = chatRoom.getTotalMember();
        this.currentUsers = chatRoom.getCurrentMember();
        // 수정 필요
        this.masterId = chatRoom.getUserChatRooms().get(0).getUser().getId();
        this.position = 1;
        // 플레이리스트 추가 필요
    }
}
