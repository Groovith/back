package com.groovith.groovith.domain.chatRoom.dto;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  채팅창 목록 조회시 request dto
 * */

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatRoomListRequestDto {
    private Long userId;

    @Builder
    public ChatRoomListRequestDto(Long userId){
        this.userId = userId;
    }

}
