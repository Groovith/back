package com.groovith.groovith.domain.message.dto;


import com.groovith.groovith.domain.chatRoom.domain.PlayerCall;
import com.groovith.groovith.domain.message.domain.Message;
import com.groovith.groovith.domain.message.domain.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * messages: [
 *     {
 *       messageId: "",
 *       chatRoomId: "",
 *       type: "CHAT | JOIN | LEAVE | PLAYER" | null,
 *       playerCall:
 *         "RESUME | PAUSE | POSITION | NEXT | PREVIOUS | ADD | REMOVE | MOVE | PLAYAT | SYNC" | null,
 *       playlistId: 0 | null,
 *       playlistIndex: 0 | null,
 *       newIndex: 0 | null,
 *       newTrack: {},
 *       position: 0 | null,
 *       senderId: "",
 *       content: "",
 *       sentAt: "0000-00-00T00:00:00Z",
 *     },
 *   ],
 *   */
@Data
public class MessageListDto {
    private Long messageId;
    private Long chatRoomId;
    private MessageType type;
    private PlayerCall playerCall;
    private Long playlistId;
    private Long playlistIndex;
    private Long newIndex;
    //private TrackDto trackDto;
    private Long position;
    private Long senderId;
    private String content;
    private LocalDateTime sentAt;

    //sentAt: "0000-00-00T00:00:00Z",
    public MessageListDto(Message message){
        this.messageId = message.getId();
        this.chatRoomId = message.getChatRoomId();
        this.senderId = message.getUserId();
        this.content = message.getContent();
        this.type = message.getMessageType();
        //this.sentAt = message.getCreatedDate();
    }
}
