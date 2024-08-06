package com.groovith.groovith.dto;

import lombok.Data;

@Data
public class InviteRequestDto {

    public Long inviterId;

    public Long inviteeId;

    public Long chatRoomId;
}
