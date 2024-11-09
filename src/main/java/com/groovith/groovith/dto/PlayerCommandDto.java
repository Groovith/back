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
    private List<TrackDto> tracks;
    private Integer index;

    @Builder
    public PlayerCommandDto(
            PlayerActionResponseType action,
            String videoId,
            Long position,
            List<TrackDto> tracks,
            Integer index) {
        this.action = action;
        this.videoId = videoId;
        this.position = position;
        this.tracks = tracks;
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

    public static PlayerCommandDto seek(Long position) {
        return PlayerCommandDto.builder()
                .action(PlayerActionResponseType.SEEK)
                .position(position)
                .build();
    }

    public static PlayerCommandDto playTrackAtIndex(int index, String videoId) {
        return PlayerCommandDto.builder()
                .action(PlayerActionResponseType.PLAY_TRACK)
                .videoId(videoId)
                .index(index)
                .build();
    }

    public static PlayerCommandDto updatePlaylist(List<TrackDto> trackDtoList) {
        return PlayerCommandDto.builder()
                .action(PlayerActionResponseType.UPDATE)
                .tracks(trackDtoList)
                .build();
    }
}
