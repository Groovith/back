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
    private ChatRoomPrivacy status;  // "PRIVATE" | "PUBLIC"
    private ChatRoomPermission permission; //

//    private ChatRoomType chatRoomType;     // "song" | "album" | "artist" | "playlist"
//    private Long songId;
//    private Long albumId;
//    private Long artistId;
//    private Long playlistId;

    public CreateChatRoomRequestDto(Long userId, String name, ChatRoomPrivacy chatRoomPrivacy, ChatRoomType chatRoomType, Long typeId) {
        //this.userId = userId;
        this.name = name;
        this.status = chatRoomPrivacy;
//        this.chatRoomType = chatRoomType;
//
//        switch (chatRoomType) {
//            case SONG:
//                this.songId = typeId;
//                break;
//            case ALBUM:
//                this.albumId = typeId;
//                break;
//            case ARTIST:
//                this.artistId = typeId;
//                break;
//            case PLAYLIST:
//                this.playlistId = typeId;
//                break;
//        }
    }
//    // ChatRoom 엔티티로 변환
//    public ChatRoom toEntity(){
//        ChatRoom chatRoom =  ChatRoom.builder()
//                            .name(this.name)
//                            .chatRoomStatus(this.status)
//                            //.chatRoomType(this.chatRoomType)
//                            .build();
//        return chatRoom;
//    }

}
