package com.groovith.groovith.service;

import com.groovith.groovith.config.WebSocketEventListener;
import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.PlayerActionRequestType;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.*;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.CurrentPlaylistRepository;
import com.groovith.groovith.repository.CurrentPlaylistTrackRepository;
import com.groovith.groovith.repository.PlayerSessionRepository;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@Service
@AllArgsConstructor
@Slf4j
public class PlayerService {

    private static final int MAX_PLAYLIST_ITEMS = 100;
    private static final int INITIAL_INDEX = 0;
    private static final long INITIAL_POSITION = 0L;
    private static final boolean INITIAL_REPEAT_SETTING = true;
    private static final int INITIAL_USER_COUNT = 1;

    private final PlayerSessionService playerSessionService;
    private final SimpMessageSendingOperations template;
    private final WebSocketEventListener webSocketEventListener;
    private final CurrentPlaylistRepository currentPlaylistRepository;
    private final CurrentPlaylistTrackRepository currentPlaylistTrackRepository;
    private final PlayerSessionRepository playerSessionRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;
    private final YoutubeService youtubeService;
    private final TrackService trackService;
    private final PlaylistService playlistService;
    private final StringRedisTemplate redisTemplate;

    public static final ConcurrentHashMap<String, Long> sessionIdChatRoomId = new ConcurrentHashMap<>(); // 각 유저 아이디의 플레이어 참가 여부

    @Transactional(readOnly = true)
    public ResponseEntity<PlayerDetailsDto> getPlayerDetails(Long chatRoomId, Long userId) {
        if (!chatRoomService.isMember(chatRoomId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);

        return getOptionalPlayerSessionByChatRoomId(chatRoomId)
                .map(playerSession -> ResponseEntity.ok().body(getPlayerDetailsDtoWithPlayerSession(chatRoomId, trackDtoList, playerSession)))
                .orElseGet(() -> ResponseEntity.ok().body(getPlayerDetailsDtoWithoutPlayerSession(chatRoomId, trackDtoList)));
    }

    @Transactional(readOnly = true)
    public PlayerDetailsDto getPlayerDetails(Long chatRoomId) {
        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);

        return getOptionalPlayerSessionByChatRoomId(chatRoomId)
                .map(playerSession -> getPlayerDetailsDtoWithPlayerSession(chatRoomId, trackDtoList, playerSession))
                .orElseGet(() -> getPlayerDetailsDtoWithoutPlayerSession(chatRoomId, trackDtoList));
    }

    /*   유저의 sessionId를 받아온다.
         sessionId를 sessionIdChatRoomId에 등록한다.
         기존의 다른 채팅방 토픽 구독은 이미 해제했을 것을 전제한다.
         chatRoomId로 세션 인원을 증가시킨다.
         chatRoomId로 세션 정보를 찾는다.
         없다면 첫 손님이므로 세션을 만들고 초기화 한다.
         * 채팅방 현재 플레이리스트는 이미 가지고 있을 것을 전제한다*/
    public PlayerDetailsDto joinPlayer(Long chatRoomId, Long userId) {
        String sessionId = getWebSocketSessionIdByUserId(userId);
        Long existingChatRoomId = sessionIdChatRoomId.get(sessionId);
        handleExistingPlayerSession(existingChatRoomId, sessionId);

        // 이미 동일한 채팅방에 참가 중이라면 인원수를 증가시키지 않음
        if (chatRoomId.equals(existingChatRoomId)) {
            PlayerSession playerSession = getPlayerSessionByChatRoomId(chatRoomId);
            List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);
            return getPlayerDetailsDtoWithPlayerSession(chatRoomId, trackDtoList, playerSession);
        }

        // sessionId를 sessionIdChatRoomId에 등록한다.
        sessionIdChatRoomId.put(sessionId, chatRoomId);

        CurrentPlaylist currentPlaylist = getCurrentPlayListByChatRoomId(chatRoomId);
        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);

        PlayerDetailsDto playerDetailsDto = getOptionalPlayerSessionByChatRoomId(chatRoomId)
                .map(playerSession -> {
                    // 플레이어 세션이 존재 할 경우: sessionId를 세션에 추가, 인원 추가
                    PlayerSession updatedSession = addSessionIdToPlayerSession(playerSession, sessionId);
                    return getPlayerDetailsDtoWithPlayerSession(chatRoomId, trackDtoList, updatedSession);
                })
                .orElseGet(() -> {
                    // 존재하지 않을 경우: 세션 생성, 메시지 전달
                    PlayerSession newSession = initializeNewPlayerSession(chatRoomId, sessionId, currentPlaylist);
                    PlayerDetailsDto dto = createPlayerDetailsDto(chatRoomId, newSession, trackDtoList);
                    sendPlayerDetailsToChatRoom(chatRoomId, dto);
                    return dto;
                });

        sendPlayerDetailsToChatRoom(chatRoomId, playerDetailsDto);
        return playerDetailsDto;
    }

    private void handleExistingPlayerSession(Long existingChatRoomId, String sessionId) {
        if(existingChatRoomId != null) {
            getOptionalPlayerSessionByChatRoomId(existingChatRoomId)
                    .ifPresent(playerSession -> deleteUserFromPlayerSession(playerSession, sessionId));
        }
    }

    @Transactional(readOnly = true)
    public void leavePlayer(Long chatRoomId, Long userId) {
        // 유저의 sessionId를 받아온다.
        String sessionId = getWebSocketSessionIdByUserId(userId);
        sessionIdChatRoomId.remove(sessionId);

        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);

        PlayerSession playerSession = getPlayerSessionByChatRoomId(chatRoomId);
        deleteUserFromPlayerSession(playerSession, sessionId);
        // 세션이 비었을 경우 삭제
        handleEmptyPlayerSession(playerSession, chatRoomId, trackDtoList);
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
                    TrackDto trackDto = saveTrack(playerRequestDto.getVideoId());
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

    @Transactional(readOnly = true)
    public void pause(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, PlayerRequestDto playerRequestDto) {
        savePlayerSession(PlayerSession.pause(playerSession, playerRequestDto.getPosition()));
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);
        PlayerCommandDto playerCommandDto = PlayerCommandDto.pause(playerRequestDto.getPosition());

        sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
    }

    @Transactional(readOnly = true)
    public void resume(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, PlayerRequestDto playerRequestDto) {
        savePlayerSession(PlayerSession.resume(playerSession, playerRequestDto.getPosition()));
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);
        PlayerCommandDto playerCommandDto = PlayerCommandDto.resume(playerRequestDto.getPosition());

        sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
    }

    @Transactional(readOnly = true)
    public void seek(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, PlayerRequestDto playerRequestDto) {
        savePlayerSession(PlayerSession.seek(playerSession, playerRequestDto.getPosition()));
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

            savePlayerSession(playerSession);
            PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);

            // 채팅방 정보 전송
            sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
        } catch (Exception e) {
            e.printStackTrace();
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

        savePlayerSession(playerSession);
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
        savePlayerSession(playerSession);
        // 현재 플레이리스트 정보 갱신
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);
        // 플레이 트랙 액션 전송
        PlayerCommandDto playerCommandDto = PlayerCommandDto.playTrackAtIndex(requestedIndex, trackDtoList.get(requestedIndex).getVideoId());

        // 채팅방 정보 전송
        sendMessages(chatRoomId, playerDetailsDto, playerCommandDto);
    }

    @Transactional
    public void addToCurrentPlaylist(PlayerSession playerSession, List<TrackDto> trackDtoList, Long chatRoomId, TrackDto trackDto) {
        CurrentPlaylist currentPlaylist = getCurrentPlayListByChatRoomId(chatRoomId);
        // 플레이리스트가 다 찼을 경우(100곡)
        validatePlaylistCapacity(currentPlaylist, trackDtoList);
        // 플레이리스트에 새로운 트랙 추가(새 연관관계 생성)
        saveTrackToCurrentPlayList(currentPlaylist, trackDto);
        trackDtoList = getTrackDtoList(chatRoomId);
        // 플레이리스트 업데이트 알림 전송: 갱신된 채팅방 정보
        sendMessages(chatRoomId
                , PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList)
                , PlayerCommandDto.updatePlaylist(trackDtoList, playerSession.getIndex()));
    }

    @Transactional
    public void removeFromCurrentPlaylist(PlayerSession playerSession, Long chatRoomId, int index) {
        // 플레이리스트에서 해당 인덱스의 트랙 삭제
        playlistService.deleteTrackByIndex(chatRoomId, index);
        List<TrackDto> trackDtoList = getTrackDtoList(chatRoomId);
        PlayerSession.removeTrack(playerSession, index);
        savePlayerSession(playerSession);
        // 재생목록에서 현재 재생중인 트랙 삭제했을때 로직 필요
        // 채팅방 정보 갱신
        PlayerDetailsDto playerDetailsDto = PlayerDetailsDto.toPlayerDetailsDto(chatRoomId, playerSession, trackDtoList);
        // 플레이리스트 업데이트 알림 전송
        sendMessages(chatRoomId, playerDetailsDto, PlayerCommandDto.updatePlaylist(trackDtoList, playerSession.getIndex()));
    }

    private void savePlayerSession(PlayerSession playerSession) {
//        playerSessionService.savePlayerSession(playerSession);
        playerSessionRepository.save(playerSession);
        addPlayerSessionToZSet(playerSession);
    }

    private PlayerSession initializeNewPlayerSession(Long chatRoomId, String sessionId, CurrentPlaylist currentPlaylist) {
        PlayerSession playerSession = createPlayerSession(chatRoomId, currentPlaylist);
        playerSession.addSessionId(sessionId);
        savePlayerSession(playerSession);
        return playerSession;
    }

    private PlayerSession addSessionIdToPlayerSession(PlayerSession playerSession, String sessionId) {
        playerSession.addSessionId(sessionId);
        playerSession.updateUserCount();
        savePlayerSession(playerSession);
        return playerSession;
    }

    private void deleteUserFromPlayerSession(PlayerSession playerSession, String sessionId) {
        playerSession.removeSessionId(sessionId);
        playerSession.updateUserCount();
        savePlayerSession(playerSession);
    }

    private TrackDto saveTrack(String videoId) throws IOException {
        TrackDto trackDto = youtubeService.getVideo(videoId);
        trackService.save(trackDto);
        return trackDto;
    }


    public void addPlayerSessionToZSet(PlayerSession playerSession) {
        playerSessionService.addPlayerSessionToZSet(playerSession);
    }

    public void removePlayerSessionFromZSet(PlayerSession playerSession) {
        playerSessionService.removePlayerSessionFromZSet(playerSession);
    }

    private void deletePlaySession(PlayerSession playerSession) {
        playerSessionRepository.delete(playerSession);
        removePlayerSessionFromZSet(playerSession);
    }

    private PlayerSession getPlayerSessionByChatRoomId(Long chatRoomId) {
        return playerSessionRepository.findById(chatRoomId)
                .orElseThrow(() -> new PlayerSessionNotFoundException(chatRoomId));
    }

    private Optional<PlayerSession> getOptionalPlayerSessionByChatRoomId(Long chatRoomId) {
        return playerSessionRepository.findById(chatRoomId);
    }

    private String getWebSocketSessionIdByUserId(Long userId) {
        return webSocketEventListener.getSessionIdByUserId(userId)
                .orElseThrow(() -> new RuntimeException("웹소켓 세션에 등록되지 않은 userId 입니다. userId: " + userId));
    }

    private ChatRoom getChatRoomByChatRoomId(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
    }


    private void handleEmptyPlayerSession(PlayerSession playerSession, Long chatRoomId, List<TrackDto> trackDtoList) {
        if (playerSession.getSessionIds().isEmpty()) {
            deletePlaySession(playerSession);
            // 채팅방에 알린다
            sendPlayerDetailsToChatRoom(chatRoomId, getPlayerDetailsDtoWithoutPlayerSession(chatRoomId, trackDtoList));
        }
    }

    private boolean isCurrentPlaylistEmpty(CurrentPlaylist currentPlaylist) {
        if (currentPlaylist.getCurrentPlaylistTracks().isEmpty()) {
            return true;
        }
        return false;
    }

//    private void validateUserPermission(ChatRoom chatRoom, Long userId) {
//        ChatRoomPermission permission = getChatRoomPermission(chatRoom);
//
//        boolean isMaster = permission.equals(ChatRoomPermission.MASTER) && isMasterUser(chatRoom, userId);
//        boolean isEveryone = permission.equals(ChatRoomPermission.EVERYONE);
//
//        if (!isMaster && !isEveryone) {
//            throw new NotMasterUserException(userId);
//        }
//    }

    private void validatePlaylistCapacity(CurrentPlaylist currentPlaylist, List<TrackDto> trackDtoList) {
        if (trackDtoList.size() >= MAX_PLAYLIST_ITEMS) {
            throw new CurrentPlayListFullException(currentPlaylist.getId());
        }
    }

    private void saveTrackToCurrentPlayList(CurrentPlaylist currentPlaylist, TrackDto trackDto) {
        Track track = new Track(trackDto);
        CurrentPlaylistTrack currentPlaylistTrack = CurrentPlaylistTrack.setPlaylistTrack(currentPlaylist, track);
        currentPlaylistTrackRepository.save(currentPlaylistTrack);
    }

    private PlayerDetailsDto createPlayerDetailsDto(Long chatRoomId, PlayerSession playerSession, List<TrackDto> trackDtoList) {
        return PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylistIndex(playerSession.getIndex())
                .lastPosition(playerSession.getLastPosition())
                .startedAt(playerSession.getStartedAt())
                .repeat(playerSession.getRepeat())
                .paused(playerSession.getPaused())
                .userCount(playerSession.getUserCount())
                .currentPlaylist(trackDtoList)
                .build();
    }

    private CurrentPlaylist getCurrentPlayListByChatRoomId(Long chatRoomId) {
        return currentPlaylistRepository.findByChatRoomId(chatRoomId).orElseThrow(() -> new PlayListNotFoundException(chatRoomId));
    }

    private PlayerSession createPlayerSession(Long chatRoomId, CurrentPlaylist currentPlaylist) {
        PlayerSession playerSession = PlayerSession.builder()
                .chatRoomId(chatRoomId)
                .index(INITIAL_INDEX)
                .lastPosition(INITIAL_POSITION)
                .paused(isCurrentPlaylistEmpty(currentPlaylist))
                .repeat(INITIAL_REPEAT_SETTING)
                .startedAt(LocalDateTime.now())
                .userCount(INITIAL_USER_COUNT)
                .duration(getDurationByCurrentPlayList(currentPlaylist))
                .build();
        return playerSession;
    }

    private Long getDurationByCurrentPlayList(CurrentPlaylist currentPlaylist) {
        if (currentPlaylist.getCurrentPlaylistTracks().isEmpty()) {
            return null;
        }
        return currentPlaylist.getCurrentPlaylistTracks().get(0).getTrack().getDuration();
    }


    private PlayerDetailsDto getPlayerDetailsDtoWithoutPlayerSession(Long chatRoomId, List<TrackDto> trackDtoList) {
        return PlayerDetailsDto.builder()
                .chatRoomId(chatRoomId)
                .currentPlaylist(trackDtoList)
                .build();
    }

    private PlayerDetailsDto getPlayerDetailsDtoWithPlayerSession(Long chatRoomId, List<TrackDto> trackDtoList, PlayerSession playerSession) {
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

    private void sendMessages(Long chatRoomId, PlayerDetailsDto playerDetailsDto, PlayerCommandDto playerCommandDto) {
        // 채팅방 정보 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
        // 같이 듣기 액션 전송
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player/listen-together", playerCommandDto);
    }

    private void sendPlayerDetailsToChatRoom(Long chatRoomId, PlayerDetailsDto playerDetailsDto) {
        template.convertAndSend("/sub/api/chatrooms/" + chatRoomId + "/player", playerDetailsDto);
    }

    private List<Track> getTracksByChatRoomId(Long chatRoomId) {
        return currentPlaylistTrackRepository.findTrackListByChatRoomId(chatRoomId);
    }

    public List<TrackDto> getTrackDtoList(Long chatRoomId) {
        List<Track> trackList = getTracksByChatRoomId(chatRoomId);
        return trackList.stream()
                .map(TrackDto::new)
                .toList();
    }
}
