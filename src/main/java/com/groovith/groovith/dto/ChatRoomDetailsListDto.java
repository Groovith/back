package com.groovith.groovith.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatRoomDetailsListDto {
    private List<ChatRoomDetailsDto> chatRooms;
}
