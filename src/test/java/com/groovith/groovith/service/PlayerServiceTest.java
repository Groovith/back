package com.groovith.groovith.service;

import com.groovith.groovith.config.WebSocketEventListener;
import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.CurrentPlaylist;
import com.groovith.groovith.domain.PlayerSession;
import com.groovith.groovith.domain.Track;
import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.ChatRoomPrivacy;
import com.groovith.groovith.domain.enums.S3Directory;
import com.groovith.groovith.dto.PlayerDetailsDto;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.CurrentPlaylistRepository;
import com.groovith.groovith.repository.CurrentPlaylistTrackRepository;
import com.groovith.groovith.repository.PlayerSessionRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @InjectMocks private  PlayerService playerService;
    @Mock private  SimpMessageSendingOperations template;
    @Mock private WebSocketEventListener webSocketEventListener;
    @Mock private CurrentPlaylistRepository currentPlaylistRepository;
    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private CurrentPlaylistTrackRepository currentPlaylistTrackRepository;
    @Mock private YoutubeService youtubeService;
    @Mock private TrackService trackService;
    @Mock private PlaylistService playlistService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private PlayerSessionRepository playerSessionRepository;


//    @Test
//    @DisplayName("세션이 존재하는 체팅방 같이듣기에 참가 테스트")
//    void joinPlayerTest(){
//        // given
//        Long chatRoomId = 1L;
//        Long userId = 1L;
//        String sessionId = UUID.randomUUID().toString();
//
//        ChatRoom chatRoom = createChatRoom(chatRoomId, "room", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER);
//        CurrentPlaylist currentPlaylist = createCurrentPlaylist(chatRoomId);
//        List<Track> trackList = Arrays.asList(new Track(), new Track(), new Track());
//        PlayerSession playerSession = createPlayerSession(chatRoomId, sessionId, currentPlaylist);
//
//        // when
//        when(webSocketEventListener.getSessionIdByUserId(userId)).thenReturn(Optional.of(sessionId));
//        when(currentPlaylistRepository.findByChatRoomId(chatRoomId)).thenReturn(Optional.of(currentPlaylist));
//        when(currentPlaylistTrackRepository.findTrackListByChatRoomId(chatRoomId)).thenReturn(trackList);
//        when(playerSessionRepository.findById(chatRoomId)).thenReturn(Optional.of(playerSession));
//
//        PlayerDetailsDto result = playerService.joinPlayer(userId, chatRoomId);
//
//        // then
//        Assertions.assertThat(result).isNotNull();
//        Assertions.assertThat(result.getChatRoomId()).isEqualTo(chatRoom);
//        Assertions.assertThat(result.getCurrentPlaylist()).isEqualTo(currentPlaylist);
//        Assertions.assertThat(result.getUserCount()).isEqualTo(2);
//    }
//
//    @Test
//    @DisplayName("플레이리스트에 곡 추가 테스트")
//    void addTrackTest(){
//        // given
//
//        // when
//
//        // then
//    }

    private CurrentPlaylist createCurrentPlaylist(Long id) {
        CurrentPlaylist currentPlaylist = new CurrentPlaylist();
        currentPlaylist.setId(id);
        return currentPlaylist;
    }

    private ChatRoom createChatRoom(Long chatRoomId, String name, ChatRoomPrivacy chatRoomPrivacy, ChatRoomPermission permission){
        ChatRoom chatRoom = ChatRoom.builder()
                .name(name)
                .privacy(chatRoomPrivacy)
                .imageUrl( S3Directory.CHATROOM.getDefaultImageUrl())
                .permission(permission)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        return chatRoom;
    }

    private PlayerSession createPlayerSession(Long chatRoomId, String sessionId, CurrentPlaylist currentPlaylist) {
        PlayerSession data = new PlayerSession();
        data.setChatRoomId(chatRoomId);
        data.addSessionId(sessionId);
        if (currentPlaylist.getCurrentPlaylistTracks().isEmpty()) {
            data.setPaused(true);
            data.setRepeat(true);
            data.setIndex(0);
        } else {
            data.setIndex(0);
            data.setPaused(false);
            data.setLastPosition(0L);
            data.setRepeat(true);
            data.setDuration(currentPlaylist.getCurrentPlaylistTracks().get(0).getTrack().getDuration());
            data.setStartedAt(LocalDateTime.now());
        }
        data.setUserCount(1);
        return data;
    }
}