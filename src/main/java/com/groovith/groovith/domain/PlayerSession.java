package com.groovith.groovith.domain;

import com.groovith.groovith.dto.SpotifyTrackDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class PlayerSession {
    private Integer index;
    private Long lastPosition;
    private Boolean paused;
    private Boolean repeat;
    private LocalDateTime startedAt;
    private AtomicInteger userCount;
    private Long duration;
}
