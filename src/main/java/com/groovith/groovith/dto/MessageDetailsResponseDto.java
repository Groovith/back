package com.groovith.groovith.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.groovith.groovith.domain.Message;
import com.groovith.groovith.domain.MessageType;
import com.groovith.groovith.domain.User;
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
public class MessageDetailsResponseDto {
    private Long messageId;
    private Long chatRoomId;
    private Long userId;
    private String username;
    private String content;
    private MessageType type;
    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;
}
