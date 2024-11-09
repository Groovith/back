package com.groovith.groovith.domain;

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

    public static PlayerSession pause(PlayerSession playerSession, Long position) {
        playerSession.setPaused(true);
        playerSession.setLastPosition(position);
        return playerSession;
    }

    public static PlayerSession resume(PlayerSession playerSession, Long position) {
        playerSession.setPaused(false);
        playerSession.setLastPosition(position);
        playerSession.setStartedAt(LocalDateTime.now());
        return playerSession;
    }
}
