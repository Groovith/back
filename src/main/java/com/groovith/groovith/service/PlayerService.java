package com.groovith.groovith.service;

import com.groovith.groovith.config.WebSocketEventListener;
import com.groovith.groovith.domain.CurrentPlaylist;
import com.groovith.groovith.domain.PlayerActionResponseType;
import com.groovith.groovith.domain.PlayerSession;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.CurrentPlayListFullException;
import com.groovith.groovith.repository.CurrentPlaylistRepository;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
public class PlayerService {
    private final SimpMessageSendingOperations template;
    private final WebSocketEventListener webSocketEventListener;
    private final CurrentPlaylistRepository currentPlaylistRepository;

    public static final ConcurrentHashMap<Long, PlayerSession> playerSessions = new ConcurrentHashMap<>(); // 채팅방 플레이어 정보 (chatRoomId, PlayerSessionDto)
    public static final ConcurrentHashMap<String, Long> sessionIdChatRoomId = new ConcurrentHashMap<>(); // 각 유저 아이디의 플레이어 참가 여부

    @Transactional(readOnly = true)
    public PlayerDetailsDto getPlayerDetails(Long chatRoomId) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) {
            // 현재 세션이 없는 경우
            return PlayerDetailsDto.builder()
                    .chatRoomId(chatRoomId)
                    .currentPlaylist(currentPlaylist.getTracks())
                    .build();
        } else {
            // 현재 세션이 있는 경우
            return PlayerDetailsDto.builder()
                    .chatRoomId(chatRoomId)
                    .currentPlaylist(currentPlaylist.getTracks())
                    .currentPlaylistIndex(playerSession.getIndex())
                    .userCount(playerSession.getUserCount().get())
                    .lastPosition(playerSession.getLastPosition())
                    .startedAt(playerSession.getStartedAt())
                    .paused(playerSession.getPaused())
                    .repeat(playerSession.getRepeat())
                    .build();
        }
    }

    public PlayerDetailsDto joinPlayer(Long chatRoomId, Long userId) {
        // 유저의 sessionId를 받아온다.
        // sessionId를 sessionIdChatRoomId에 등록한다.
        // 기존의 다른 채팅방 토픽 구독은 이미 해제했을 것을 전제한다.
        // chatRoomId로 세션 인원을 증가시킨다.
        // chatRoomId로 세션 정보를 찾는다.
        // 없다면 첫 손님이므로 세션을 만들고 초기화 한다.
        // * 채팅방 현재 플레이리스트는 이미 가지고 있을 것을 전제한다.
        String sessionId = webSocketEventListener.getSessionIdByUserId(userId).orElseThrow(() -> new RuntimeException("웹소켓 세션에 등록되지 않은 userId 입니다. userId: " + userId));
        // 유저가 이미 어떤 채팅방에 참가 중인지 확인
        Long existingChatRoomId = sessionIdChatRoomId.get(sessionId);

        // 이미 동일한 채팅방에 참가 중이라면 인원수를 증가시키지 않음
        if (chatRoomId.equals(existingChatRoomId)) {
            PlayerSession playerSession = playerSessions.get(chatRoomId);
            CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
            return PlayerDetailsDto.builder()
                    .chatRoomId(chatRoomId)
                    .currentPlaylistIndex(playerSession.getIndex())
                    .lastPosition(playerSession.getLastPosition())
                    .startedAt(playerSession.getStartedAt())
                    .repeat(playerSession.getRepeat())
                    .paused(playerSession.getPaused())
                    .userCount(playerSession.getUserCount().get())
                    .currentPlaylist(currentPlaylist.getTracks())
                    .build();
        }

        // sessionId를 sessionIdChatRoomId에 등록한다.
        sessionIdChatRoomId.put(sessionId, chatRoomId);

        // 플레이어 세션을 불러온다. 없다면 새로 생성한다. 있다면 현재 인원을 증가시킨다.
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();

        if (playerSession == null) {
            PlayerSession newSession = new PlayerSession();
            // 현재 플레이리스트에 곡이 있다면 처음 곡으로 설정한다. 없다면 그대로 둔다.
            if (currentPlaylist.getTracks().isEmpty()) {
                newSession.setPaused(true);
                newSession.setRepeat(false);
                newSession.setIndex(0);
            } else {
                newSession.setIndex(0);
                newSession.setPaused(false);
                newSession.setLastPosition(0L);
                newSession.setRepeat(false);
                newSession.setDuration(currentPlaylist.getTracks().get(0).getDuration_ms());
                newSession.setStartedAt(LocalDateTime.now());
            }
            playerSessions.put(chatRoomId, newSession);

            newSession.setUserCount(new AtomicInteger(1));

            PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                    .chatRoomId(chatRoomId)
                    .currentPlaylistIndex(newSession.getIndex())
                    .lastPosition(newSession.getLastPosition())
                    .startedAt(newSession.getStartedAt())
                    .repeat(newSession.getRepeat())
                    .paused(newSession.getPaused())
                    .userCount(newSession.getUserCount().get())
                    .currentPlaylist(currentPlaylist.getTracks())
                    .build();

            // 채팅방에 알린다
            template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);

            return playerDetailsDto;

        } else {
            playerSession.getUserCount().incrementAndGet();
        }

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylistIndex(playerSession.getIndex())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .repeat(playerSession.getRepeat())
                .paused(playerSession.getPaused())
                .userCount(playerSession.getUserCount().get())
                .currentPlaylist(currentPlaylist.getTracks())
                .build();

        // 채팅방에 알린다
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);

        return playerDetailsDto;
    }

    @Transactional(readOnly = true)
    public void leavePlayer(Long chatRoomId, Long userId) {
        // 유저의 sessionId를 받아온다.
        String sessionId = webSocketEventListener.getSessionIdByUserId(userId)
                .orElseThrow(() -> new RuntimeException("웹소켓 세션에 등록되지 않은 userId 입니다. userId: " + userId));
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow(() -> new RuntimeException(chatRoomId + "의 현재 플레이리스트가 없습니다."));

        // sessionIdChatRoomId 에서 sessionId를 삭제한다.
        sessionIdChatRoomId.remove(sessionId);

        // chatRoomId로 세션 인원을 감소시킨다.
        AtomicInteger count = playerSessions.get(chatRoomId).getUserCount();
        if (count != null) {
            int currentUserCount = count.decrementAndGet();
            if (currentUserCount <= 0) {
                // 인원이 0명이 된 경우 해당 세션을 삭제한다.
                playerSessions.remove(chatRoomId);
                System.out.println("채팅방 " + chatRoomId + " 의 플레이어에 참가자가 없어서 세션이 삭제되었습니다.");

                // 채팅방에 알린다
                PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                        .chatRoomId(chatRoomId)
                        .currentPlaylist(currentPlaylist.getTracks())
                        .build();

                template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
            }
        }
    }

    @Transactional
    public void handleMessage(Long chatRoomId, PlayerRequestDto playerRequestDto) {
        // 채팅방 플레이어 세션에 메시지를 받으면 채팅방을 조회하는 유저들과 같이 듣기를 하고 있는 유저들에게 각각 따로 메시지를 전달한다.
        switch (playerRequestDto.getAction()) {
            case PLAY_NEW_TRACK -> playNewTrack(chatRoomId, playerRequestDto);
            case PAUSE -> pause(chatRoomId, playerRequestDto);
            case RESUME -> resume(chatRoomId, playerRequestDto);
            case SEEK -> seek(chatRoomId, playerRequestDto);
            case NEXT_TRACK -> nextTrack(chatRoomId);
            case PREVIOUS_TRACK -> previousTrack(chatRoomId);
            case PLAY_AT_INDEX -> playAtIndex(chatRoomId, playerRequestDto);
            case ADD_TO_CURRENT_PLAYLIST -> {
                if (playerRequestDto.getTrack() != null) {
                    addToCurrentPlaylist(chatRoomId, playerRequestDto.getTrack());
                }
            }
            case REMOVE_FROM_CURRENT_PLAYLIST -> {
                if (playerRequestDto.getIndex() != null) {
                    removeFromCurrentPlaylist(chatRoomId, playerRequestDto.getIndex());
                }
            }
            case TRACK_ENDED -> {}
        }
    }

    @Transactional
    public void playNewTrack(Long chatRoomId, PlayerRequestDto playerRequestDto) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        // 플레이리스트 수정
        List<SpotifyTrackDto> newTracks = new ArrayList<>();
        newTracks.add(playerRequestDto.getTrack());
        currentPlaylist.setTracks(newTracks);
        currentPlaylistRepository.save(currentPlaylist);

        // 플레이어 세션 수정
        playerSession.setIndex(0);
        playerSession.setPaused(false);
        playerSession.setLastPosition(0L);
        playerSession.setStartedAt(LocalDateTime.now());
        playerSession.setDuration(playerRequestDto.getTrack().getDuration_ms());
        playerSessions.put(chatRoomId, playerSession);

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        PlayerResponseDto playerResponseDto = PlayerResponseDto.builder()
                .action(PlayerActionResponseType.PLAY_TRACK)
                .track(playerRequestDto.getTrack())
                .position(null)
                .build();


        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerResponseDto);
    }

    @Transactional(readOnly = true)
    public void pause(Long chatRoomId, PlayerRequestDto playerRequestDto) {
        // 정지, 위치 또한 조정
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        playerSession.setPaused(true);
        playerSession.setLastPosition(playerRequestDto.getPosition());
        playerSessions.put(chatRoomId, playerSession);

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        PlayerResponseDto playerResponseDto = PlayerResponseDto.builder()
                .action(PlayerActionResponseType.PAUSE)
                .track(null)
                .position(playerRequestDto.getPosition())
                .build();


        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerResponseDto);
    }

    @Transactional(readOnly = true)
    public void resume(Long chatRoomId, PlayerRequestDto playerRequestDto) {
        // 정지, 위치 또한 조정
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        playerSession.setPaused(false);
        playerSession.setLastPosition(playerRequestDto.getPosition());
        playerSession.setStartedAt(LocalDateTime.now());
        playerSessions.put(chatRoomId, playerSession);

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        PlayerResponseDto playerResponseDto = PlayerResponseDto.builder()
                .action(PlayerActionResponseType.RESUME)
                .track(null)
                .position(playerRequestDto.getPosition())
                .build();


        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerResponseDto);
    }

    @Transactional(readOnly = true)
    public void seek(Long chatRoomId, PlayerRequestDto playerRequestDto) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        playerSession.setLastPosition(playerRequestDto.getPosition());
        playerSession.setStartedAt(LocalDateTime.now());
        playerSessions.put(chatRoomId, playerSession);

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        PlayerResponseDto playerResponseDto = PlayerResponseDto.builder()
                .action(PlayerActionResponseType.SEEK)
                .track(null)
                .position(playerRequestDto.getPosition())
                .build();


        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerResponseDto);
    }

    @Transactional(readOnly = true)
    public void nextTrack(Long chatRoomId) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        PlayerResponseDto playerResponseDto;

        int nextIndex = playerSession.getIndex() + 1;
        if (nextIndex < currentPlaylist.getTracks().size()) {
            // 다음 곡이 있는 경우
            playerSession.setIndex(nextIndex);
            playerSession.setLastPosition(0L);
            playerSession.setPaused(false);
            playerSession.setStartedAt(LocalDateTime.now());
            playerSession.setDuration(currentPlaylist.getTracks().get(nextIndex).getDuration_ms());

            playerResponseDto = PlayerResponseDto.builder()
                    .action(PlayerActionResponseType.PLAY_TRACK)
                    .track(currentPlaylist.getTracks().get(nextIndex))
                    .index(nextIndex)
                    .build();

        } else {
            // 다음 곡이 없는 경우
            if (playerSession.getRepeat()) {
                // 반복 재생이 설정되어 있는 경우
                playerSession.setIndex(0);
                playerSession.setLastPosition(0L);
                playerSession.setPaused(false);
                playerSession.setStartedAt(LocalDateTime.now());
                playerSession.setDuration(currentPlaylist.getTracks().get(0).getDuration_ms());

                playerResponseDto = PlayerResponseDto.builder()
                        .action(PlayerActionResponseType.PLAY_TRACK)
                        .track(currentPlaylist.getTracks().get(0))
                        .index(0)
                        .build();
            } else {
                // 반복 재생이 설정되어 있지 않은 경우 -> 딱히 뭐 하지 않음
                playerResponseDto = PlayerResponseDto.builder()
                        .build();
            }
        }

        playerSessions.put(chatRoomId, playerSession);

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerResponseDto);
    }

    @Transactional(readOnly = true)
    public void previousTrack(Long chatRoomId) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        PlayerResponseDto playerResponseDto;

        int prevIndex = playerSession.getIndex() - 1;
        if (prevIndex >= 0) {
            // 이전 곡이 있는 경우
            playerSession.setIndex(prevIndex);
            playerSession.setLastPosition(0L);
            playerSession.setPaused(false);
            playerSession.setStartedAt(LocalDateTime.now());
            playerSession.setDuration(currentPlaylist.getTracks().get(prevIndex).getDuration_ms());

            playerResponseDto = PlayerResponseDto.builder()
                    .action(PlayerActionResponseType.PLAY_TRACK)
                    .track(currentPlaylist.getTracks().get(prevIndex))
                    .index(prevIndex)
                    .build();

        } else {
            // 이전 곡이 없는 경우
            if (playerSession.getRepeat()) {
                // 반복 재생이 설정되어 있는 경우
                int lastIndex = currentPlaylist.getTracks().size() - 1;
                playerSession.setIndex(lastIndex);
                playerSession.setLastPosition(0L);
                playerSession.setPaused(false);
                playerSession.setStartedAt(LocalDateTime.now());
                playerSession.setDuration(currentPlaylist.getTracks().get(lastIndex).getDuration_ms());

                playerResponseDto = PlayerResponseDto.builder()
                        .action(PlayerActionResponseType.PLAY_TRACK)
                        .track(currentPlaylist.getTracks().get(lastIndex))
                        .index(lastIndex)
                        .build();
            } else {
                // 반복 재생이 설정되어 있지 않은 경우 -> 딱히 뭐 하지 않음
                playerResponseDto = PlayerResponseDto.builder()
                        .build();
            }
        }

        playerSessions.put(chatRoomId, playerSession);

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerResponseDto);
    }

    @Transactional
    public void playAtIndex(Long chatRoomId, PlayerRequestDto playerRequestDto) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        // 인덱스 범위 확인
        Integer requestedIndex = playerRequestDto.getIndex();
        if (requestedIndex == null || requestedIndex < 0 || requestedIndex >= currentPlaylist.getTracks().size()) {
            return;
        }

        // 플레이어 세션 수정
        playerSession.setIndex(requestedIndex);
        playerSession.setPaused(false);
        playerSession.setLastPosition(0L);
        playerSession.setStartedAt(LocalDateTime.now());
        playerSession.setDuration(currentPlaylist.getTracks().get(requestedIndex).getDuration_ms());
        playerSessions.put(chatRoomId, playerSession);

        // 현재 플레이리스트 정보 갱신
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        // 플레이 트랙 액션 전송
        PlayerResponseDto playerResponseDto = PlayerResponseDto.builder()
                .action(PlayerActionResponseType.PLAY_TRACK)
                .track(currentPlaylist.getTracks().get(requestedIndex))
                .index(requestedIndex)
                .position(0L) // 새로운 트랙 재생 시작이므로 위치는 0
                .build();

        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerResponseDto);
    }

    @Transactional
    public void addToCurrentPlaylist(Long chatRoomId, SpotifyTrackDto track) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        // 플레이리스트에 새로운 트랙 추가
        List<SpotifyTrackDto> updatedTracks = new ArrayList<>(currentPlaylist.getTracks());

        // 플레이리스트가 다 찼을 경우(100곡)
        if(currentPlaylist.getTracks().size() >= 100){
            throw new CurrentPlayListFullException(currentPlaylist.getId());
        }

        updatedTracks.add(track);
        currentPlaylist.setTracks(updatedTracks);
        currentPlaylistRepository.save(currentPlaylist);

        // 채팅방 정보 갱신
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        // 플레이리스트 업데이트 알림 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together",
                PlayerResponseDto.builder()
                        .action(PlayerActionResponseType.UPDATE)
                        .currentPlaylist(currentPlaylist.getTracks())
                        .index(playerSession.getIndex())
                        .build());
    }

    @Transactional
    public void removeFromCurrentPlaylist(Long chatRoomId, int index) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        if (playerSession == null) return;

        // 인덱스 범위 확인
        List<SpotifyTrackDto> tracks = currentPlaylist.getTracks();
        if (index < 0 || index >= tracks.size()) return;

        // 플레이리스트에서 해당 인덱스의 트랙 삭제
        tracks.remove(index);
        currentPlaylist.setTracks(tracks);
        currentPlaylistRepository.save(currentPlaylist);

        // 만약 현재 재생 중인 트랙이 삭제된 트랙보다 뒤에 있다면 인덱스를 조정
        if (playerSession.getIndex() >= index) {
            playerSession.setIndex(Math.max(0, playerSession.getIndex() - 1));
        }
        playerSessions.put(chatRoomId, playerSession);

        // 채팅방 정보 갱신
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(currentPlaylist.getTracks())
                .currentPlaylistIndex(playerSession.getIndex())
                .userCount(playerSession.getUserCount().get())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .paused(playerSession.getPaused())
                .repeat(playerSession.getRepeat())
                .build();

        // 플레이리스트 업데이트 알림 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together",
                PlayerResponseDto.builder()
                        .action(PlayerActionResponseType.UPDATE)
                        .currentPlaylist(currentPlaylist.getTracks())
                        .index(playerSession.getIndex())
                        .build());
    }

    @Scheduled(fixedRate = 1000)  // 1초마다 실행
    public void updatePosition() {
        for (Map.Entry<Long, PlayerSession> entry : playerSessions.entrySet()) {
            Long chatRoomId = entry.getKey();
            PlayerSession playerSession = entry.getValue();

            if (!playerSession.getPaused()) {
                long elapsedMillis = Duration.between(playerSession.getStartedAt(), LocalDateTime.now()).toMillis();
                long currentPosition = playerSession.getLastPosition() + elapsedMillis;

                if (currentPosition >= playerSession.getDuration() - 1500) {
                    // 곡의 duration 을 초과한 경우 다음 트랙으로 이동
                    nextTrack(chatRoomId);
                } else {
                    // 아직 duration 을 초과하지 않았다면 position 업데이트
                    playerSession.setLastPosition(currentPosition);
                    playerSession.setStartedAt(LocalDateTime.now());
                }
            }
        }
    }

//    @Scheduled(fixedRate = 1000)
//    @Transactional(readOnly = true)
//    public void updatePlayerDetails() {
//        // 정지되지 않은 모든 플레이어 가져오기
//        List<Player> players = playerRepository.findAllByPausedFalse().orElseThrow();
//
//        for (Player player : players) {
//            // 현재 재생 상태를 WebSocket 으로 전송
//            template.convertAndSend("/sub/api/chatrooms/" + player.getChatRoomId() + "/player", new PlayerDetailsDto(player));
//        }
//    }

    //    @Scheduled(fixedRate = 1000) // 매 초마다 실행
//    @Transactional
//    public void updatePlayerPositions() {
    // 정지되지 않은 모든 플레이어 가져오기
//        List<Player> players = playerRepository.findAllByPausedFalse().orElseThrow();
//
//        for (Player player : players) {
//            // 플레이어의 position 을 1000ms 증가
//            player.setPosition(player.getPosition() + 1000);
//
//            // position 이 duration 을 초과하면 다음 곡으로 이동
//            if (player.getPosition() >= player.getDuration()) {
//                nextTrack(player.getChatRoomId());
//            } else {
//                // 그렇지 않으면 플레이어 상태만 업데이트
//                playerRepository.save(player);
//            }
//
//            // 현재 재생 상태를 WebSocket 으로 전송
//            PlayerDetailsDto playerDetailsDto = new PlayerDetailsDto(player);
//            template.convertAndSend("/sub/api/chatrooms/" + player.getChatRoomId() + "/player", playerDetailsDto);
//        }
//    }
// }


//
//
//
//    @Transactional
//    public PlayerDetailsDto playAtIndex(Long chatRoomId, int index) {
//        Player player = playerRepository.findByChatRoomId(chatRoomId).orElseThrow(() -> new RuntimeException("player with chatRoomId(" + chatRoomId + ") is null"));
//
//        List<SpotifyTrackDto> currentPlaylist = player.getCurrentPlaylist();
//        if (index < 0 || index >= currentPlaylist.size()) {
//            throw new RuntimeException("index(" + index + ") out of bound. currentPlaylist.size(" + currentPlaylist.size() + ")");
//        }
//        player.setCurrentPlaylistIndex(index);
//        player.setPaused(false);
//        player.setPosition(0L);
//        player.setDuration(currentPlaylist.get(index).getDuration_ms());
//        playerRepository.save(player);
//        return new PlayerDetailsDto(player);
//    }
//

//
//
//

}
