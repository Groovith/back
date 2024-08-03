package com.groovith.groovith.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.groovith.groovith.domain.PlayerCall;
import com.groovith.groovith.domain.Message;
import com.groovith.groovith.domain.MessageType;
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
    private Long userId;
    private String username;
    private String content;
    private MessageType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

//    private PlayerCall playerCall;
//    private Long playlistId;
//    private Long playlistIndex;
//    private Long newIndex;
//    private TrackDto trackDto;
//    private Long position;
//    private Long senderId;

    //sentAt: "0000-00-00T00:00:00Z",
    public MessageListDto(Message message){
        this.messageId = message.getId();
        this.chatRoomId = message.getChatRoom().getId();
        this.userId = message.getUserId();
        this.content = message.getContent();
        this.type = message.getMessageType();
        this.createdAt = message.getCreatedAt();
    }
}
