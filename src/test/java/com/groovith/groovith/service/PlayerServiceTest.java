////package com.groovith.groovith.service;
////
////import com.groovith.groovith.config.WebSocketEventListener;
////import com.groovith.groovith.domain.CurrentPlaylist;
////import com.groovith.groovith.domain.PlayerSession;
////import com.groovith.groovith.dto.PlayerDetailsDto;
//<<<<<<< HEAD
////import com.groovith.groovith.repository.CurrentPlaylistRepository;
////import org.junit.jupiter.api.DisplayName;
//=======
////import com.groovith.groovith.repository.ChatRoomRepository;
////import com.groovith.groovith.repository.CurrentPlaylistRepository;
////import com.groovith.groovith.service.PlayerService;
////import org.junit.jupiter.api.BeforeEach;
//>>>>>>> origin/main
////import org.junit.jupiter.api.Test;
////import org.junit.jupiter.api.extension.ExtendWith;
////import org.mockito.InjectMocks;
////import org.mockito.Mock;
//<<<<<<< HEAD
//=======
////import org.mockito.MockitoAnnotations;
//>>>>>>> origin/main
////import org.mockito.junit.jupiter.MockitoExtension;
////import org.springframework.messaging.simp.SimpMessageSendingOperations;
////
////import java.util.Optional;
//<<<<<<< HEAD
////import java.util.concurrent.ConcurrentHashMap;
////
////import static org.junit.jupiter.api.Assertions.*;
////import static org.mockito.Mockito.*;
////
////@ExtendWith(MockitoExtension.class)
////
////class PlayerServiceTest {
////
////    @InjectMocks PlayerService playerService;
////    @Mock CurrentPlaylistRepository currentPlaylistRepository;
////    @Mock SimpMessageSendingOperations template;
////    @Mock WebSocketEventListener webSocketEventListener;
////
////    @Test
////    @DisplayName("플레이어 세션 참가 테스트")
////    public void joinPlayer(){
////        //given
////        Long chatRoomId = 1L;
////        Long userId = 1L;
////
////        final ConcurrentHashMap<Long, PlayerSession> playerSessions = new ConcurrentHashMap<>(); // 채팅방 플레이어 정보 (chatRoomId, PlayerSessionDto)
////        final ConcurrentHashMap<String, Long> sessionIdChatRoomId = new ConcurrentHashMap<>(); // 각 유저 아이디의 플레이어 참가 여부
////
////        PlayerSession newSession = new PlayerSession();
////        CurrentPlaylist currentPlaylist = new CurrentPlaylist();
////
//////        PlayerDetailsDto dto = PlayerDetailsDto.builder()
//////                .chatRoomId(chatRoomId)
//////                .currentPlaylistIndex(newSession.getIndex())
//////                .lastPosition(newSession.getLastPosition())
//////                .startedAt(newSession.getStartedAt())
//////                .repeat(newSession.getRepeat())
//////                .paused(newSession.getPaused())
//////                .userCount(newSession.getUserCount().get())
//////                .videoList(currentPlaylist.getVideoList())
//////                .build();
////
////
////        //when
////        when(webSocketEventListener.getSessionIdByUserId(userId))
////                .thenReturn(Optional.of("1L"));
////        when(currentPlaylistRepository.findByChatRoomId(anyLong()))
////                .thenReturn(Optional.of(currentPlaylist));
////        PlayerDetailsDto result = playerService.joinPlayer(chatRoomId, userId);
////        //then
////        System.out.println(result);
////
////    }
////
////
////}
//=======
////import java.util.concurrent.atomic.AtomicInteger;
////
////import static org.junit.jupiter.api.Assertions.assertEquals;
////import static org.mockito.ArgumentMatchers.anyLong;
////import static org.mockito.Mockito.*;
////
////@ExtendWith(MockitoExtension.class)
////class PlayerServiceTest {
////
////    @Mock private WebSocketEventListener webSocketEventListener;
////    @Mock private SimpMessageSendingOperations template;
////    @Mock private CurrentPlaylistRepository currentPlaylistRepository;
////    @Mock private ChatRoomRepository chatRoomRepository;
////    @InjectMocks private PlayerService playerService;
////
////    @BeforeEach
////    void setUp() {
////        MockitoAnnotations.openMocks(this);
////        playerService.playerSessions.clear();
////    }
////
////    @Test
////    void testJoinPlayer_NewSession() {
////        // Given
////        Long chatRoomId = 1L;
////        Long userId = 2L;
////        String sessionId = "sessionId";
////        CurrentPlaylist currentPlaylist = new CurrentPlaylist();
////
////
////        // When
////        when(currentPlaylistRepository.findByChatRoomId(anyLong())).thenReturn(Optional.of(currentPlaylist));
////        when(webSocketEventListener.getSessionIdByUserId(anyLong())).thenReturn(Optional.of(sessionId));
////        PlayerDetailsDto result = playerService.joinPlayer(chatRoomId, userId);
////
////        // Then
////        assertEquals(chatRoomId, result.getChatRoomId());
////        assertEquals(1, result.getUserCount());
////
////        PlayerSession playerSession = playerService.playerSessions.get(chatRoomId);
////        assertEquals(1, playerSession.getUserCount().get());
////        assertEquals(0, playerSession.getIndex());
////    }
////
////    @Test
////    void testJoinPlayer_ExistingSession() {
////        // Given
////        Long chatRoomId = 1L;
////        Long userId = 2L;
////        CurrentPlaylist currentPlaylist = new CurrentPlaylist();
////
////        PlayerSession existingSession = new PlayerSession();
////        existingSession.setUserCount(new AtomicInteger(1));
////
////        // When
////        playerService.playerSessions.put(chatRoomId, existingSession);
////        when(currentPlaylistRepository.findByChatRoomId(anyLong())).thenReturn(Optional.of(currentPlaylist));
////
////        PlayerDetailsDto result = playerService.joinPlayer(chatRoomId, userId);
////
////        // Then
////        assertEquals(chatRoomId, result.getChatRoomId());
////        assertEquals(2, result.getUserCount());
////
////        PlayerSession playerSession = playerService.playerSessions.get(chatRoomId);
////        assertEquals(2, playerSession.getUserCount().get());
////    }
////}
//>>>>>>> origin/main
