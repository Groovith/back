package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.PlayerActionResponseType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PlayerCommandDto {
    private PlayerActionResponseType action;
    private String videoId;
    private Long position;
    private List<TrackDto> videoList;
    private Integer index;

    @Builder
    public PlayerCommandDto(
            PlayerActionResponseType action,
            String videoId,
            Long position,
            List<TrackDto> videoList,
            Integer index) {
        this.action = action;
        this.videoId = videoId;
        this.position = position;
        this.videoList = videoList;
        this.index = index;
    }

    public static PlayerCommandDto pause(Long position) {
        return PlayerCommandDto.builder()
                .action(PlayerActionResponseType.PAUSE)
                .position(position)
                .build();
    }

    public static PlayerCommandDto resume(Long position) {
        return PlayerCommandDto.builder()
                .action(PlayerActionResponseType.RESUME)
                .position(position)
                .build();
    }
}
