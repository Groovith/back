package com.groovith.groovith.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchChatRoomsResponseDto {
    private final List<ChatRoomDetailDto> chatRooms;
}
