package com.groovith.groovith.config;

import com.groovith.groovith.domain.CurrentPlaylist;
import com.groovith.groovith.domain.PlayerSession;
import com.groovith.groovith.domain.Track;
import com.groovith.groovith.domain.PlayerSession;
import com.groovith.groovith.dto.PlayerDetailsDto;
import com.groovith.groovith.dto.TrackDto;
import com.groovith.groovith.exception.PlayerSessionNotFoundException;
import com.groovith.groovith.repository.CurrentPlaylistRepository;
import com.groovith.groovith.repository.CurrentPlaylistTrackRepository;
import com.groovith.groovith.repository.PlayerSessionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.groovith.groovith.service.PlayerService.sessionIdChatRoomId;

@Component
@AllArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private static final ConcurrentHashMap<Long, String> userIdSessionId = new ConcurrentHashMap<>();
    private final SimpMessageSendingOperations template;
    private final CurrentPlaylistTrackRepository currentPlaylistTrackRepository;
    private final PlayerSessionRepository playerSessionRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionSubscribeEvent event) {
        // 여기서 사용자 연결 정보를 저장한다
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Long userId = (Long) Objects.requireNonNull(accessor.getSessionAttributes()).get("userId");
        userIdSessionId.put(userId, Objects.requireNonNull(sessionId));
    }

    @Transactional(readOnly = true)
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // 여기서 연결 해제 된 사용자를 찾아 조치한다
        // 해당 사용자가 참가하고 있던 플레이어 세션 관계를 찾아 삭제한다
        // sessionId로 chatRoomId를 찾는다 -> chatRoomId로 인원 수를 찾아 수정한다.
            // 세션의 인원 수가 0명이면 세션을 삭제한다 (chatRoomId로 찾는다)

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        // sessionId로 userId를 찾고, 해당 userId를 삭제합니다.
        Long userId = userIdSessionId.entrySet()
                .stream()
                .filter(entry -> Objects.requireNonNull(sessionId).equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (userId != null) {
            userIdSessionId.remove(userId);
            // sessionId로 chatRoomId를 찾는다.
            Long chatRoomId = sessionIdChatRoomId.remove(sessionId);
            PlayerSession playerSession = playerSessionRepository.findById(chatRoomId)
                    .orElseThrow(()->new PlayerSessionNotFoundException(chatRoomId));
            if (chatRoomId != null) {
                // chatRoomId로 현재 인원 수를 줄인다.
                int count = playerSession.getUserCount();
                // playerSession의 sessionIds 에서 sessionId 제거
                playerSession.removeSessionId(sessionId);
                playerSession.updateUserCount();
                    // 인원이 0명이 된 경우, 해당 채팅방 세션 정보를 삭제한다. -> 해당 채팅방에 알린다
                    if (count <= 0) {
                        playerSessionRepository.delete(playerSession);

                        List<Track> trackList = currentPlaylistTrackRepository.findTrackListByChatRoomId(chatRoomId);
                        List<TrackDto> trackDtoList = trackList.stream()
                                .map(TrackDto::new)
                                .toList();

                        // 채팅방에 알린다
                        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                                .chatRoomId(chatRoomId)
                                .currentPlaylist(trackDtoList)
                                .build();

                        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
                    }
            }
        }
    }

    // userId 로 sessionId 를 반환 받는다
    public Optional<String> getSessionIdByUserId(Long userId) {
        return Optional.ofNullable(userIdSessionId.get(userId));
    }
}
