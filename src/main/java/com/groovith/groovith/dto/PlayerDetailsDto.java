package com.groovith.groovith.dto;

import com.groovith.groovith.domain.PlayerSession;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Slf4j
public class PlayerDetailsDto {
    private Long id;
    private Long chatRoomId;
    private Integer userCount;
    private Integer currentPlaylistIndex;
    private List<TrackDto> currentPlaylist;
    private Boolean paused;
    private Boolean repeat;
    private Long position;
    private Long duration;

    @Builder
    public PlayerDetailsDto(
            Long chatRoomId,
            Integer userCount,
            Integer currentPlaylistIndex,
            List<TrackDto> currentPlaylist,
            Boolean paused,
            Boolean repeat,
            Long lastPosition,
            LocalDateTime startedAt) {
        this.chatRoomId = chatRoomId;
        this.userCount = userCount;
        this.currentPlaylistIndex = currentPlaylistIndex;
        this.currentPlaylist = currentPlaylist;
        this.paused = paused;
        this.repeat = repeat;
        this.position = lastPosition;

        // 실행 중인 경우 현재 position 계산
        if (paused != null && !paused) {
            if (startedAt != null) {
                long durationSeconds = Duration.between(startedAt, LocalDateTime.now()).toSeconds();
                this.position = lastPosition + durationSeconds;
            }
        }
    }

    public static PlayerDetailsDto toPlayerDetailsDto(Long chatRoomId, PlayerSession playerSession, List<TrackDto> trackDtoList) {
        return PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(trackDtoList)
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();
    }
}
