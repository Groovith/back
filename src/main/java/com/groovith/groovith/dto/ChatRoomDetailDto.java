package com.groovith.groovith.dto;


import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomStatus;
import com.groovith.groovith.domain.ChatRoomType;
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

// chatroom 상세조회 dto
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
    private ChatRoomType type;
    private ChatRoomStatus status;
    // position paused

    public ChatRoomDetailDto(ChatRoom chatRoom){
        this.chatRoomId = chatRoom.getId();
        this.name = chatRoom.getName();
        this.totalUsers = chatRoom.getTotalMember();
        this.currentUsers = chatRoom.getCurrentMember();
        this.status = chatRoom.getStatus();
        this.type = chatRoom.getType();
        // 채팅방 안에 사람이 있을때만
        if (totalUsers > 0){
            this.masterId = chatRoom.getMasterId();
        }
        // 플레이리스트 추가 필요
    }
}
