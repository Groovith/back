package com.groovith.groovith.dto;

import com.groovith.groovith.domain.PlayerSession;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
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
                this.position = lastPosition + Duration.between(startedAt, LocalDateTime.now()).toSeconds();
            }
        }
    }

    public static PlayerDetailsDto pause(Long chatRoomId, List<TrackDto> trackDtoList, PlayerSession playerSession) {
        return PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(trackDtoList)
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();
    }

    public static PlayerDetailsDto resume(Long chatRoomId, List<TrackDto> trackDtoList, PlayerSession playerSession) {
        return PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(trackDtoList)
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();
    }
}
