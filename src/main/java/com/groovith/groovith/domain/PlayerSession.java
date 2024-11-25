package com.groovith.groovith.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Data
@NoArgsConstructor
@RedisHash("PlayerSession")
public class PlayerSession implements Serializable {
    @Id
    private Long chatRoomId;
    private Integer index;
    private Long lastPosition;
    private Boolean paused;
    private Boolean repeat;
    private LocalDateTime startedAt;
    private int userCount;
    private Long duration;

    private Set<String> sessionIds = new HashSet<>();

    @Builder
    public PlayerSession(final Long chatRoomId, final Integer index, final Long lastPosition, Boolean paused, final Boolean repeat, final LocalDateTime startedAt, final int userCount, final Long duration) {
        this.chatRoomId = chatRoomId;
        this.index = index;
        this.lastPosition = lastPosition;
        this.paused = paused;
        this.repeat = repeat;
        this.startedAt = startedAt;
        this.userCount = userCount;
        this.duration = duration;
    }

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

    public static PlayerSession seek(PlayerSession playerSession, Long position) {
        playerSession.setLastPosition(position);
        playerSession.setStartedAt(LocalDateTime.now());
        return playerSession;
    }

    public static void changeTrack(PlayerSession playerSession, int nextIndex, Long duration) {
        playerSession.setIndex(nextIndex);
        playerSession.setLastPosition(0L);
        playerSession.setPaused(false);
        playerSession.setStartedAt(LocalDateTime.now());
        playerSession.setDuration(duration);
    }

    public static void returnToStart(PlayerSession playerSession, Long duration) {
        playerSession.setIndex(0);
        playerSession.setLastPosition(0L);
        playerSession.setPaused(false);
        playerSession.setStartedAt(LocalDateTime.now());
        playerSession.setDuration(duration);
    }

    public static void removeTrack(PlayerSession playerSession, int index) {
        // 만약 현재 재생 중인 트랙이 삭제된 트랙보다 뒤에 있다면 인덱스를 조정
        if (playerSession.getIndex() >= index) {
            playerSession.setIndex(Math.max(0, playerSession.getIndex() - 1));
        }
    }

    public void addSessionId(String sessionId) {
        this.sessionIds.add(sessionId);
    }

    public void removeSessionId(String sessionId) {
        this.sessionIds.remove(sessionId);
    }
    public void increaseUserCount() {
        this.userCount++;
    }

    public void decreaseUserCount() {
        this.userCount--;
    }
}
