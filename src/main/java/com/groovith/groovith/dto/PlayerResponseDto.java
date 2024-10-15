package com.groovith.groovith.dto;

import com.groovith.groovith.domain.PlayerActionResponseType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PlayerResponseDto {
    private PlayerActionResponseType action;
    private String videoId;
    private Long position;
    private List<String> videoList;
    private Integer index;

    @Builder
    public PlayerResponseDto(
            PlayerActionResponseType action,
            String videoId,
            Long position,
            List<String> videoList,
            Integer index) {
        this.action = action;
        this.videoId = videoId;
        this.position = position;
        this.videoList = videoList;
        this.index = index;
    }
}
