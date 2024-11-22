package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.ChatRoomPrivacy;
import com.groovith.groovith.domain.enums.ChatRoomType;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  ChatRoom 생성 request dto
 * */
@Getter
@NoArgsConstructor
public class CreateChatRoomRequestDto {
    //private Long userId;
    private String name;
    private ChatRoomPrivacy privacy;  // "PRIVATE" | "PUBLIC"
    private ChatRoomPermission permission; //

//    private ChatRoomType chatRoomType;     // "song" | "album" | "artist" | "playlist"
//    private Long songId;
//    private Long albumId;
//    private Long artistId;
//    private Long playlistId;
}
