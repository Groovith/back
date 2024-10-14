package com.groovith.groovith.dto;

import lombok.Data;

import java.util.List;

@Data
public class InviteFriendsRequestDto {
    // 초대할 친구 user_id 들
    private List<Long> friends;
}
