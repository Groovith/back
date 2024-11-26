package com.groovith.groovith.service;

import com.groovith.groovith.config.WebSocketEventListener;
import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.PlayerActionRequestType;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.exception.CurrentPlayListFullException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.CurrentPlaylistRepository;
import com.groovith.groovith.repository.CurrentPlaylistTrackRepository;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@AllArgsConstructor
@Slf4j
public class PlayerService {
    private final SimpMessageSendingOperations template;
    private final WebSocketEventListener webSocketEventListener;
    private final CurrentPlaylistRepository currentPlaylistRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final CurrentPlaylistTrackRepository currentPlaylistTrackRepository;
    private final YoutubeService youtubeService;
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final StringRedisTemplate redisTemplate;
    private static final int MAX_PLAYLIST_ITEMS = 100;

    public static final ConcurrentHashMap<Long, PlayerSession> playerSessions = new ConcurrentHashMap<>(); // 채팅방 플레이어 정보 (chatRoomId, PlayerSessionDto)
    public static final ConcurrentHashMap<String, Long> sessionIdChatRoomId = new ConcurrentHashMap<>(); // 각 유저 아이디의 플레이어 참가 여부

    @Transactional(readOnly = true)
    public PlayerDetailsDto getPlayerDetails(Long chatRoomId) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);
        if (playerSession == null) {
            // 현재 세션이 없는 경우
            return PlayerDetailsDto.builder()
                    .chatRoomId(chatRoomId)
                    .currentPlaylist(trackDtoList)
                    .build();
        } else {
            // 현재 세션이 있는 경우
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
            List<TrackDto> trackDtoList = currentPlaylist.getCurrentPlaylistTracks().stream()
                    .map(currentPlaylistTrack -> new TrackDto(currentPlaylistTrack.getTrack()))
                    .toList();
            return PlayerDetailsDto.builder()
                    .chatRoomId(chatRoomId)
                    .currentPlaylistIndex(playerSession.getIndex())
                    .lastPosition(playerSession.getLastPosition())
                    .startedAt(playerSession.getStartedAt())
                    .repeat(playerSession.getRepeat())
                    .paused(playerSession.getPaused())
                    .userCount(playerSession.getUserCount().get())
                    .currentPlaylist(trackDtoList)
                    .build();
        }

        // sessionId를 sessionIdChatRoomId에 등록한다.
        sessionIdChatRoomId.put(sessionId, chatRoomId);

        // 플레이어 세션을 불러온다. 없다면 새로 생성한다. 있다면 현재 인원을 증가시킨다.
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        List<TrackDto> trackDtoList = currentPlaylist.getCurrentPlaylistTracks().stream()
                .map(currentPlaylistTrack -> new TrackDto(currentPlaylistTrack.getTrack()))
                .toList();

        if (playerSession == null) {
            PlayerSession newSession = new PlayerSession();
            // 현재 플레이리스트에 곡이 있다면 처음 곡으로 설정한다. 없다면 그대로 둔다.
            if (currentPlaylist.getCurrentPlaylistTracks().isEmpty()) {
                // 세션 생성시에 반복재생 설정
                newSession.setPaused(true);
                newSession.setRepeat(true);
                newSession.setIndex(0);
            } else {
                newSession.setIndex(0);
                newSession.setPaused(false);
                newSession.setLastPosition(0L);
                newSession.setRepeat(true);
                newSession.setDuration(currentPlaylist.getCurrentPlaylistTracks().get(0).getTrack().getDuration());
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
                    .currentPlaylist(trackDtoList)
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
                .currentPlaylist(trackDtoList)
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
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new RuntimeException(chatRoomId + "의 현재 플레이리스트가 없습니다."));
        List<TrackDto> trackDtoList = currentPlaylist.getCurrentPlaylistTracks().stream()
                .map(currentPlaylistTrack -> new TrackDto(currentPlaylistTrack.getTrack()))
                .toList();
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
                        .currentPlaylist(trackDtoList)
                        .build();

                template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
            }
        }
    }

    @Transactional
    public void handleMessage(Long chatRoomId, PlayerRequestDto playerRequestDto, Long userId) throws IOException {
        // 채팅방 플레이어 세션에 메시지를 받으면 채팅방을 조회하는 유저들과 같이 듣기를 하고 있는 유저들에게 각각 따로 메시지를 전달한다.
        ChatRoomPermission permission = getChatRoomPermission(chatRoomId);
        boolean isMasterUser = isMasterUser(chatRoomId, userId);
        if (playerRequestDto.getAction() == PlayerActionRequestType.TRACK_ENDED) {
            // 트랙 정지의 경우 무조건 처리한다
            trackEnded(chatRoomId);
        } else if ((permission.equals(ChatRoomPermission.MASTER) && isMasterUser)
                || permission.equals(ChatRoomPermission.EVERYONE)) {
            // masterUser 만 플레이어 조작 가능 or 권한이 모두 인 경우
            // 채팅방 플레이어 세션에 메시지를 받으면 채팅방을 조회하는 유저들과 같이 듣기를 하고 있는 유저들에게 각각 따로 메시지를 전달한다.
            handleActionAndSendMessages(playerRequestDto.getAction(), chatRoomId, playerRequestDto);
        }
    }

    private ChatRoomPermission getChatRoomPermission(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
        return chatRoom.getPermission();
    }

    private boolean isMasterUser(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
        return chatRoom.getMasterUserId().equals(userId);
    }

    private void handleActionAndSendMessages(PlayerActionRequestType action, Long chatRoomId, PlayerRequestDto playerRequestDto) throws IOException {
        PlayerSession playerSession = getPlayerSessionByChatRoomId(chatRoomId);
        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);
        switch (action) {
            case PAUSE -> pause(playerSession, trackDtoList, chatRoomId, playerRequestDto);
            case RESUME -> resume(playerSession, trackDtoList, chatRoomId, playerRequestDto);
            case SEEK -> seek(playerSession, trackDtoList, chatRoomId, playerRequestDto);
            case NEXT_TRACK -> nextTrack(playerSession, trackDtoList, chatRoomId);
            case PREVIOUS_TRACK -> previousTrack(playerSession, trackDtoList, chatRoomId);
            case PLAY_AT_INDEX -> playAtIndex(playerSession, trackDtoList, chatRoomId, playerRequestDto);
            case ADD_TO_CURRENT_PLAYLIST -> {
                if (playerRequestDto.getVideoId() != null) {
                    TrackDto trackDto = youtubeService.getVideo(playerRequestDto.getVideoId());
                    trackService.save(trackDto);
                    addToCurrentPlaylist(playerSession, trackDtoList, chatRoomId, trackDto);
                }
            }
            case REMOVE_FROM_CURRENT_PLAYLIST -> {
                if (playerRequestDto.getIndex() != null) {
                    removeFromCurrentPlaylist(playerSession, chatRoomId, playerRequestDto.getIndex());
                }
            }
            default -> throw new ValidationException("Invalid action type.");
        }
    }

    private void trackEnded(Long chatRoomId) {
        PlayerSession playerSession = getPlayerSessionByChatRoomId(chatRoomId);
        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);

        String lockKey = "trackEndedLock:" + chatRoomId;
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(5));

        if (Boolean.TRUE.equals(lockAcquired)) {
            nextTrack(playerSession, trackDtoList, chatRoomId);
        }
    }

    @Transactional(readOnly = true)
    public void pause(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, PlayerRequestDto playerRequestDto) {
        playerSessions.put(chatRoomId, PlayerSession.pause(playerSession, playerRequestDto.getPosition()));

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);
        PlayerCommandDto playerCommandDto = PlayerCommandDto.pause(playerRequestDto.getPosition());

        sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
    }

    @Transactional(readOnly = true)
    public void resume(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, PlayerRequestDto playerRequestDto) {
        playerSessions.put(chatRoomId, PlayerSession.resume(playerSession, playerRequestDto.getPosition()));

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);
        PlayerCommandDto playerCommandDto = PlayerCommandDto.resume(playerRequestDto.getPosition());

        sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
    }

    @Transactional(readOnly = true)
    public void seek(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, PlayerRequestDto playerRequestDto) {
        playerSessions.put(chatRoomId, PlayerSession.seek(playerSession, playerRequestDto.getPosition()));

        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);
        PlayerCommandDto playerCommandDto = PlayerCommandDto.seek(playerRequestDto.getPosition());

        // 채팅방 정보 전송
        sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
    }

    @Transactional(readOnly = true)
    public void nextTrack(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId) {
        try {
            PlayerCommandDto playerCommandDto;

            int nextIndex = playerSession.getIndex() + 1;
            if (nextIndex < trackDtoList.size()) {
                // 다음 곡이 있는 경우
                log.info("Play Next Track: {}", nextIndex);
                PlayerSession.changeTrack(playerSession, nextIndex, trackDtoList.get(nextIndex).getDuration());
                playerCommandDto = PlayerCommandDto.playTrackAtIndex(nextIndex, trackDtoList.get(nextIndex).getVideoId());
            } else {
                // 다음 곡이 없는 경우
                if (playerSession.getRepeat()) {
                    // 반복 재생이 설정되어 있는 경우
                    PlayerSession.returnToStart(playerSession, trackDtoList.get(0).getDuration());
                    playerCommandDto = PlayerCommandDto.playTrackAtIndex(0, trackDtoList.get(0).getVideoId());
                } else {
                    // 반복 재생이 설정되어 있지 않은 경우 -> 플레이어 정지
                    playerCommandDto = PlayerCommandDto.stop();
                }
            }

            playerSessions.put(chatRoomId, playerSession);
            PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);

            // 채팅방 정보 전송
            sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public void previousTrack(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId) {
        PlayerCommandDto playerCommandDto;

        int prevIndex = playerSession.getIndex() - 1;
        if (prevIndex >= 0) {
            // 이전 곡이 있는 경우
            PlayerSession.changeTrack(playerSession, prevIndex, trackDtoList.get(prevIndex).getDuration());
            playerCommandDto = PlayerCommandDto.playTrackAtIndex(prevIndex, trackDtoList.get(prevIndex).getVideoId());
        } else {
            // 이전 곡이 없는 경우
            if (playerSession.getRepeat()) {
                // 반복 재생이 설정되어 있는 경우
                int lastIndex = trackDtoList.size() - 1;
                PlayerSession.changeTrack(playerSession, lastIndex, trackDtoList.get(lastIndex).getDuration());
                playerCommandDto = PlayerCommandDto.playTrackAtIndex(lastIndex, trackDtoList.get(lastIndex).getVideoId());
            } else {
                // 반복 재생이 설정되어 있지 않은 경우 -> 딱히 뭐 하지 않음
                playerCommandDto = PlayerCommandDto.builder()
                        .build();
            }
        }

        playerSessions.put(chatRoomId, playerSession);
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);

        // 채팅방 정보 전송
        sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
    }

    @Transactional
    public void playAtIndex(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, PlayerRequestDto playerRequestDto) {
        // 인덱스 범위 확인
        Integer requestedIndex = playerRequestDto.getIndex();
        if (requestedIndex == null || requestedIndex < 0 || requestedIndex >= trackDtoList.size()) {
            throw new RuntimeException("Requested index: " + requestedIndex + " is out of range: " + (trackDtoList.size() - 1));
        }

        // 플레이어 세션 수정
        PlayerSession.changeTrack(playerSession, requestedIndex, trackDtoList.get(requestedIndex).getDuration());
        playerSessions.put(chatRoomId, playerSession);

        // 현재 플레이리스트 정보 갱신
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);

        // 플레이 트랙 액션 전송
        PlayerCommandDto playerCommandDto = PlayerCommandDto.playTrackAtIndex(requestedIndex, trackDtoList.get(requestedIndex).getVideoId());

        // 채팅방 정보 전송
        sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
    }

    @Transactional
    public void addToCurrentPlaylist(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, TrackDto trackDto) {
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow();
        // 플레이리스트가 다 찼을 경우(100곡)
        if (trackDtoList.size() >= MAX_PLAYLIST_ITEMS) {
            throw new CurrentPlayListFullException(currentPlaylist.getId());
        }

        // 플레이리스트에 새로운 트랙 추가(새 연관관계 생성)
        Track track = new Track(trackDto);
        CurrentPlaylistTrack currentPlaylistTrack = CurrentPlaylistTrack.setPlaylistTrack(currentPlaylist, track);
        currentPlaylistTrackRepository.save(currentPlaylistTrack);
        trackDtoList = getTrackDtoList(chatRoomId);

        // 채팅방 정보 갱신
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);

        // 플레이리스트 업데이트 알림 전송
        sendMessages(chatRoomId, playerDetailsDto, PlayerCommandDto.updatePlaylist(trackDtoList, playerSession.getIndex()));
    }

    @Transactional
    public void removeFromCurrentPlaylist(PlayerSession playerSession, Long chatRoomId, int index) {
        // 플레이리스트에서 해당 인덱스의 트랙 삭제
        playlistService.deleteTrackByIndex(chatRoomId, index);
        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);

        PlayerSession.removeTrack(playerSession, index);
        playerSessions.put(chatRoomId, playerSession);

        // 채팅방 정보 갱신
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);

        // 플레이리스트 업데이트 알림 전송
        sendMessages(chatRoomId, playerDetailsDto, PlayerCommandDto.updatePlaylist(trackDtoList, playerSession.getIndex()));
    }

    private void sendMessages(Long chatRoomId, PlayerDetailsDto playerDetailsDto, PlayerCommandDto playerCommandDto) {
        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerCommandDto);
    }

    private PlayerSession getPlayerSessionByChatRoomId(Long chatRoomId) {
        PlayerSession playerSession = playerSessions.get(chatRoomId);
        if (playerSession == null) throw new RuntimeException("No Session with chatRoomId: " + chatRoomId);
        return playerSession;
    }

    @Transactional
    public List<TrackDto> getTrackDtoList(Long chatRoomId) {
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Playlist not found"));

        return currentPlaylist.getCurrentPlaylistTracks().stream()
                .map(c -> new TrackDto(c.getTrack()))
                .toList();
    }
}
