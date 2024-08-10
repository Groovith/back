package com.groovith.groovith.dto;

import com.groovith.groovith.domain.PlayerActionResponseType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class PlayerResponseDto {
    private PlayerActionResponseType action;
    private SpotifyTrackDto track;
    private Long position;
    private List<SpotifyTrackDto> currentPlaylist;
    private Integer index;

    @Builder
    public PlayerResponseDto(
            PlayerActionResponseType action,
            SpotifyTrackDto track,
            Long position,
            List<SpotifyTrackDto> currentPlaylist,
            Integer index) {
        this.action = action;
        this.track = track;
        this.position = position;
        this.currentPlaylist = currentPlaylist;
        this.index = index;
    }
}
