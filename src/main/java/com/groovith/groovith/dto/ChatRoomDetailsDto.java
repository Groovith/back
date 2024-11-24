package com.groovith.groovith.dto;


import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.ChatRoomPrivacy;
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
public class ChatRoomDetailsDto {

    private Long chatRoomId;
    private String name;
    private String imageUrl;
    private Long masterUserId;
    private String masterUserName;
    private ChatRoomPrivacy privacy;
    private ChatRoomPermission permission;
    private Integer currentMemberCount;
    private Boolean isMaster;

    public ChatRoomDetailsDto(ChatRoom chatRoom, boolean isMaster) {
        this.chatRoomId = chatRoom.getId();
        this.name = chatRoom.getName();
        this.imageUrl = chatRoom.getImageUrl();
        this.masterUserId = chatRoom.getMasterUserId();
        this.masterUserName = chatRoom.getMasterUserName();
        this.privacy = chatRoom.getPrivacy();
        this.permission = chatRoom.getPermission();
        this.currentMemberCount = chatRoom.getCurrentMemberCount();
        this.isMaster = isMaster;
    }
}
