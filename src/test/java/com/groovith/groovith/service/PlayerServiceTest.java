package com.groovith.groovith.service;

import com.groovith.groovith.config.WebSocketEventListener;
import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.CurrentPlaylist;
import com.groovith.groovith.domain.PlayerSession;
import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.ChatRoomPrivacy;
import com.groovith.groovith.domain.enums.S3Directory;
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

import java.time.LocalDateTime;
import java.util.UUID;

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


    @Test
    @DisplayName("redis 세션 데이터 저장 테스트")
    void savePlayerSession(){
        // given
        Long chatRoomId = 1L;
        ChatRoom chatRoom = createChatRoom(chatRoomId, "room", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER);
        CurrentPlaylist currentPlaylist = createCurrentPlaylist(chatRoomId);
        String sessionId = UUID.randomUUID().toString();
        PlayerSession playerSession = new PlayerSession();

        // when
        PlayerSession savedPlayerSession = playerSessionRepository.save(createPlayerSession(chatRoomId, sessionId, currentPlaylist));

        // then
        Assertions.assertThat(savedPlayerSession.getChatRoomId()).isEqualTo(chatRoomId);
    }

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
        return data;
    }
}