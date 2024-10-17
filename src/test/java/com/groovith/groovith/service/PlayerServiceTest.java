//package com.groovith.groovith.service;
//
//import com.groovith.groovith.config.WebSocketEventListener;
//import com.groovith.groovith.domain.CurrentPlaylist;
//import com.groovith.groovith.domain.PlayerSession;
//import com.groovith.groovith.dto.PlayerDetailsDto;
//import com.groovith.groovith.repository.CurrentPlaylistRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.messaging.simp.SimpMessageSendingOperations;
//
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//
//class PlayerServiceTest {
//
//    @InjectMocks PlayerService playerService;
//    @Mock CurrentPlaylistRepository currentPlaylistRepository;
//    @Mock SimpMessageSendingOperations template;
//    @Mock WebSocketEventListener webSocketEventListener;
//
//    @Test
//    @DisplayName("플레이어 세션 참가 테스트")
//    public void joinPlayer(){
//        //given
//        Long chatRoomId = 1L;
//        Long userId = 1L;
//
//        final ConcurrentHashMap<Long, PlayerSession> playerSessions = new ConcurrentHashMap<>(); // 채팅방 플레이어 정보 (chatRoomId, PlayerSessionDto)
//        final ConcurrentHashMap<String, Long> sessionIdChatRoomId = new ConcurrentHashMap<>(); // 각 유저 아이디의 플레이어 참가 여부
//
//        PlayerSession newSession = new PlayerSession();
//        CurrentPlaylist currentPlaylist = new CurrentPlaylist();
//
////        PlayerDetailsDto dto = PlayerDetailsDto.builder()
////                .chatRoomId(chatRoomId)
////                .currentPlaylistIndex(newSession.getIndex())
////                .lastPosition(newSession.getLastPosition())
////                .startedAt(newSession.getStartedAt())
////                .repeat(newSession.getRepeat())
////                .paused(newSession.getPaused())
////                .userCount(newSession.getUserCount().get())
////                .videoList(currentPlaylist.getVideoList())
////                .build();
//
//
//        //when
//        when(webSocketEventListener.getSessionIdByUserId(userId))
//                .thenReturn(Optional.of("1L"));
//        when(currentPlaylistRepository.findByChatRoomId(anyLong()))
//                .thenReturn(Optional.of(currentPlaylist));
//        PlayerDetailsDto result = playerService.joinPlayer(chatRoomId, userId);
//        //then
//        System.out.println(result);
//
//    }
//
//
//}