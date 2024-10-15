package com.groovith.groovith.config;

import com.google.api.client.googleapis.GoogleUtils;
import com.groovith.groovith.domain.CurrentPlaylist;
import com.groovith.groovith.dto.PlayerDetailsDto;
import com.groovith.groovith.repository.CurrentPlaylistRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.groovith.groovith.service.PlayerService.playerSessions;
import static com.groovith.groovith.service.PlayerService.sessionIdChatRoomId;

@Component
@AllArgsConstructor
public class WebSocketEventListener {
    private static final ConcurrentHashMap<Long, String> userIdSessionId = new ConcurrentHashMap<>();
    private final SimpMessageSendingOperations template;
    private final CurrentPlaylistRepository currentPlaylistRepository;

    @EventListener
    public void handleWebSocketConnectListener(SessionSubscribeEvent event) {
        // 여기서 사용자 연결 정보를 저장한다
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Long userId = (Long) Objects.requireNonNull(accessor.getSessionAttributes()).get("userId");
        System.out.println("사용자 연결 됨: userId = " + userId + ", sessionId = " + sessionId);
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
            System.out.println("사용자 연결 해제 됨: sessionId = " + sessionId + ", userId = " + userId);

            // sessionId로 chatRoomId를 찾는다.
            Long chatRoomId = sessionIdChatRoomId.remove(sessionId);

            if (chatRoomId != null) {
                // chatRoomId로 현재 인원 수를 줄인다.
                AtomicInteger count = playerSessions.get(chatRoomId).getUserCount();
                if (count != null) {
                    int currentUserCount = count.decrementAndGet();
                    System.out.println("채팅방 " + chatRoomId + " 플레이어 세션에 현재 " + currentUserCount + " 명 참여 중입니다.");

                    // 인원이 0명이 된 경우, 해당 채팅방 세션 정보를 삭제한다. -> 해당 채팅방에 알린다
                    if (currentUserCount <= 0) {
                        playerSessions.remove(chatRoomId);
                        System.out.println("채팅방 " + chatRoomId + " 의 플레이어에 참가자가 없어서 세션이 삭제되었습니다.");

                        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();

                        // 채팅방에 알린다
                        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                                .chatRoomId(chatRoomId)
                                .videoList(currentPlaylist.getVideoList())
                                .build();

                        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
                    }
                }
            }
        } else {
            System.out.println("해당 sessionId에 대응하는 userId가 없습니다. sessionId = " + sessionId);
        }
    }

    // userId 로 sessionId 를 반환 받는다
    public Optional<String> getSessionIdByUserId(Long userId) {
        return Optional.ofNullable(userIdSessionId.get(userId));
    }
}
