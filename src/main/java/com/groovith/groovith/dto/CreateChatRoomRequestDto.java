package com.groovith.groovith.dto;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomPermission;
import com.groovith.groovith.domain.ChatRoomStatus;
import com.groovith.groovith.domain.ChatRoomType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  ChatRoom 생성 request dto
 * */
@Data
@NoArgsConstructor
public class CreateChatRoomRequestDto {
    //private Long userId;
    private String name;
    private ChatRoomStatus status;  // "PRIVATE" | "PUBLIC"
    private ChatRoomPermission permission; //

//    private ChatRoomType chatRoomType;     // "song" | "album" | "artist" | "playlist"
//    private Long songId;
//    private Long albumId;
//    private Long artistId;
//    private Long playlistId;

    public CreateChatRoomRequestDto(Long userId, String name, ChatRoomStatus chatRoomStatus, ChatRoomType chatRoomType, Long typeId) {
        //this.userId = userId;
        this.name = name;
        this.status = chatRoomStatus;
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
