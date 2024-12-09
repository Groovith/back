package com.groovith.groovith.service;

import com.groovith.groovith.domain.PlayerSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class PlayerSessionService {

    private static final String PLAYER_SESSION_ZSET_KEY = "PlayerSessionSortedByUserCount";
    private static final String PLAYER_SESSION_KEY = "PlayerSession:";

    private final RedisTemplate<String, Object> stringRedisTemplate;
//    private final PlayerSessionRepository playerSessionRepository;
//
//    public void savePlayerSession(PlayerSession playerSession) {
//        redisTemplate.opsForValue().set(PLAYER_SESSION_KEY+playerSession.getChatRoomId(), playerSession);
//    }
//
//    public PlayerSession getPlayerSessionByChatRoomId(Long chatRoomId) {
//        return (PlayerSession) redisTemplate.opsForValue().get(PLAYER_SESSION_KEY+chatRoomId);
//    }

    public void addPlayerSessionToZSet(PlayerSession playerSession) {
        stringRedisTemplate.opsForZSet().add(
                PLAYER_SESSION_ZSET_KEY,
                playerSession.getChatRoomId(),
                playerSession.getUserCount());
    }

    public void removePlayerSessionFromZSet(PlayerSession playerSession) {
        stringRedisTemplate.opsForZSet().remove(
                PLAYER_SESSION_ZSET_KEY,
                playerSession.getChatRoomId());
    }


    public Set<ZSetOperations.TypedTuple<Object>> getTopChatRoomIdsByUserCountWithScores(int min, int max) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(PLAYER_SESSION_ZSET_KEY, min, max - 1);
    }
}
