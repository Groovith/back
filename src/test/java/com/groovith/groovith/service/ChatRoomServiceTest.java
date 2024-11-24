package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.*;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.ChatRoomFullException;
import com.groovith.groovith.exception.NotMasterUserException;
import com.groovith.groovith.repository.*;
import com.groovith.groovith.service.Image.ChatRoomImageService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {
    private static final String DEFAULT_IMG_URL = "defaultImgUrl";

    @InjectMocks private ChatRoomService chatRoomService;
    @Mock private ChatRoomRepository chatRoomRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserChatRoomRepository userChatRoomRepository;
    @Mock private CurrentPlaylistRepository currentPlaylistRepository;
    @Mock private FriendRepository friendRepository;
    @Mock private ChatRoomImageService chatRoomImageService;
    @Mock private MessageRepository messageRepository;

    @Test
    @DisplayName("채팅방 생성 테스트, 채팅방 생성시 유저와 연관관계 생겨야함")
    public void create(){
        //given
        Long userId = 1L;
        String chatRoomName = "testRoom";
        String userName = "masterUserName";
        String imageUrl = "imageUrl";
        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto();
        ReflectionTestUtils.setField(requestDto, "name", chatRoomName);
        ReflectionTestUtils.setField(requestDto, "status", ChatRoomPrivacy.PUBLIC);
        ReflectionTestUtils.setField(requestDto, "permission", ChatRoomPermission.MASTER);


        User user = new User();
        user.setId(userId);
        user.setUsername(userName);

        ChatRoom expectedChatRoom = createChatRoom(chatRoomName, ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);

        //when
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(expectedChatRoom);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(currentPlaylistRepository.save(any())).thenReturn(any());

        ChatRoom actualChatRoom = chatRoomService.create(userId, requestDto, imageUrl);

        //then
        Assertions.assertThat(actualChatRoom).isEqualTo(expectedChatRoom);
        //User - ChatRoom 이 연관관계가 생기는지 테스트
        Assertions.assertThat(user.getUserChatRoom().get(0).getChatRoom())
                .isEqualTo(actualChatRoom);
        //masterUser 정보 저장 테스트
        Assertions.assertThat(actualChatRoom.getMasterUserId()).isEqualTo(user.getId());
        Assertions.assertThat(actualChatRoom.getMasterUserName()).isEqualTo(user.getUsername());
    }

    @Test
    @DisplayName("채팅방 수정 테스트")
    void updateChatRoom(){
        // given
        Long chatRoomId = 1L;
        Long userId = 1L;
        String chatRoomName = "room";
        String updatedChatRoomName = "updatedRoom";
        String userName = "masterUserName";
        ChatRoom chatRoom =createChatRoom(chatRoomName, ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        User masterUser = createUser(userId, userName, DEFAULT_IMG_URL);
        // masterUser 설정
        chatRoom.setMasterUserInfo(masterUser);

        String newImageUrl = "newImageUrl";
        UpdateChatRoomRequestDto updateChatRoomRequestDto = new UpdateChatRoomRequestDto();
        ReflectionTestUtils.setField(updateChatRoomRequestDto, "name", updatedChatRoomName);
        ReflectionTestUtils.setField(updateChatRoomRequestDto, "status", ChatRoomPrivacy.PRIVATE);
        ReflectionTestUtils.setField(updateChatRoomRequestDto, "permission", ChatRoomPermission.EVERYONE);

        // when
        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        chatRoomService.updateChatRoom(chatRoomId, userId, updateChatRoomRequestDto, newImageUrl);

        // then
        Assertions.assertThat(chatRoom.getName()).isEqualTo(updatedChatRoomName);
        Assertions.assertThat(chatRoom.getPrivacy()).isEqualTo(ChatRoomPrivacy.PRIVATE);
        Assertions.assertThat(chatRoom.getPermission()).isEqualTo(ChatRoomPermission.EVERYONE);
        Assertions.assertThat(chatRoom.getImageUrl()).isEqualTo(newImageUrl);
    }


    @Test
    @DisplayName("채팅방 목록 조회 테스트")
    public void findAllDesc(){
        //given
        ChatRoom chatRoom1 = createChatRoom("room1", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ChatRoom chatRoom2 = createChatRoom("room2", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ChatRoom chatRoom3 = createChatRoom("room3", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);

        List<ChatRoom> data = new ArrayList<>();
        data.add(chatRoom1);
        data.add(chatRoom2);
        data.add(chatRoom3);

        //when
        when(chatRoomRepository.findAllDesc()).thenReturn(data);
        List<ChatRoomListResponseDto> actualChatRoomList = chatRoomService.findAllDesc();

        //then
        List<String> expectedNames = List.of("room1", "room2", "room3");
        Assertions.assertThat(actualChatRoomList)
                .extracting(ChatRoomListResponseDto::getChatRoomName)
                .containsExactlyElementsOf(expectedNames);
    }

    @Test
    @DisplayName("채팅방 현재 멤버 조회 테스트")
    void findChatRoomMemberTest() {
        // given
        Long userId = 100L;
        User user = createUser(userId, "user", DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(user, "id", userId);

        Long user1Id = 1L;
        User user1 = createUser(user1Id, "user1", "imageUrl1");
        ReflectionTestUtils.setField(user1, "id", user1Id);
        ReflectionTestUtils.setField(user1, "role", "ROLE_USER");
        Friend.setFriend(user, user1);

        Long user2Id = 2L;
        User user2 = createUser(user2Id, "user2", "imageUrl2");
        ReflectionTestUtils.setField(user2, "id", user2Id);
        ReflectionTestUtils.setField(user2, "role", "ROLE_USER");

        Long user3Id = 3L;
        User user3 = createUser(user3Id, "user3", "imageUrl3");
        ReflectionTestUtils.setField(user3, "id", user3Id);
        ReflectionTestUtils.setField(user3, "role", "ROLE_USER");

        Long chatRoomId = 1L;
        ChatRoom chatRoom = createChatRoom("room", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);
        UserChatRoom userChatRoom1 = UserChatRoom.setUserChatRoom(user1, chatRoom, UserChatRoomStatus.ENTER);
        UserChatRoom userChatRoom2 = UserChatRoom.setUserChatRoom(user2, chatRoom, UserChatRoomStatus.ENTER);
        UserChatRoom userChatRoom3 = UserChatRoom.setUserChatRoom(user3, chatRoom, UserChatRoomStatus.ENTER);
        List<UserChatRoom> enterUserChatRooms = Arrays.asList(userChatRoom, userChatRoom1, userChatRoom2, userChatRoom3);

        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        // when
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(friendRepository.findFriendsIdsFromUser(user))
                .thenReturn(List.of(1L));
        when(userChatRoomRepository.findEnterUserChatRoomsByChatRoomId(chatRoomId, UserChatRoomStatus.ENTER))
                .thenReturn(enterUserChatRooms);
        List<ChatRoomMemberDto> findMembers = chatRoomService.findChatRoomMembers(chatRoomId, user.getId());

        // then
        Assertions.assertThat(findMembers.size()).isEqualTo(4);
        assertThatFindMembers(findMembers, 0, user);
        assertThatFindMembers(findMembers, 1, user1);
        assertThatFindMembers(findMembers, 2, user2);
        assertThatFindMembers(findMembers, 3, user3);
        Assertions.assertThat(findMembers.get(1).getUserRelationship()).isEqualTo(UserRelationship.FRIEND);
    }

    private void assertThatFindMembers(List<ChatRoomMemberDto> findMembers, int index, User user){
        Assertions.assertThat(findMembers.get(index).getId()).isEqualTo(user.getId());
        Assertions.assertThat(findMembers.get(index).getUsername()).isEqualTo(user.getUsername());
        Assertions.assertThat(findMembers.get(index).getImageUrl()).isEqualTo(user.getImageUrl());

    }

    @Test
    @DisplayName("채팅방 멤버 조회 시 UserChatRoom이 Enter 인 멤버만 조회되야함")
    void findChatRoomMembers_WithStatusEnterOnly(){
        // given
        Long userId = 100L;
        User user = createUser(userId, "user", DEFAULT_IMG_URL);

        Long user1Id = 1L;
        User user1 = createUser(userId, "user1", "imageUrl1");
        ReflectionTestUtils.setField(user1, "id", user1Id);
        ReflectionTestUtils.setField(user1, "role", "ROLE_USER");

        Long user2Id = 2L;
        User user2 = createUser(user2Id, "user2", "imageUrl2");
        ReflectionTestUtils.setField(user2, "id", user2Id);
        ReflectionTestUtils.setField(user2, "role", "ROLE_USER");

        Long user3Id = 3L;
        User user3 = createUser(user3Id, "user3", "imageUrl3");
        ReflectionTestUtils.setField(user3, "id", user3Id);
        ReflectionTestUtils.setField(user3, "role", "ROLE_USER");

        Long chatRoomId = 1L;
        ChatRoom chatRoom = createChatRoom("room", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        UserChatRoom ucr = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);
        UserChatRoom ucr1 = UserChatRoom.setUserChatRoom(user1, chatRoom, UserChatRoomStatus.ENTER);
        UserChatRoom ucr2 = UserChatRoom.setUserChatRoom(user2, chatRoom, UserChatRoomStatus.LEAVE);
        UserChatRoom ucr3 = UserChatRoom.setUserChatRoom(user3, chatRoom, UserChatRoomStatus.LEAVE);
        List<UserChatRoom> enterUserChatRooms = Arrays.asList(ucr, ucr1);
        // when
        when(userChatRoomRepository.findEnterUserChatRoomsByChatRoomId(chatRoomId, UserChatRoomStatus.ENTER))
                .thenReturn(enterUserChatRooms);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(friendRepository.findFriendsIdsFromUser(user))
                .thenReturn(new ArrayList<>()); // 친구 없는 상태
        List<ChatRoomMemberDto> findMembers = chatRoomService.findChatRoomMembers(chatRoomId, user.getId());

        // then
        Assertions.assertThat(findMembers.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("채팅방 상세조회 테스트")
    public void findChatRoomDetail(){
        //given
        Long chatroomId = 1L;
        Long userId = 1L;
        int currentMemberCount = 3;
        String chatRoomName = "room";
        String masterUserName = "masterUserName";
        User user = createUser(userId,masterUserName,DEFAULT_IMG_URL);
        ChatRoom chatRoom = createChatRoom(chatRoomName, ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        // ReflectionTestUtils 사용하면 필드값 임의로 지정가능
        ReflectionTestUtils.setField(chatRoom, "id", chatroomId);
        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", currentMemberCount);
        // 유저와 연관관계 설정
        UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);
        // 마스터 유저 설정
        chatRoom.setMasterUserInfo(user);

        ChatRoomDetailsDto dto = new ChatRoomDetailsDto(chatRoom, chatRoom.getIsMaster(userId));
        dto.setChatRoomId(chatroomId);

        //when
        when(chatRoomRepository.findById(anyLong()))
                .thenReturn(Optional.of(chatRoom));

        ChatRoomDetailsDto chatRoomDetailsDto = chatRoomService.findChatRoomDetail(chatroomId, userId);

        //then
        Assertions.assertThat(chatRoomDetailsDto.getChatRoomId()).isEqualTo(chatroomId);
        Assertions.assertThat(chatRoomDetailsDto.getName()).isEqualTo(chatRoomName);
        Assertions.assertThat(chatRoomDetailsDto.getMasterUserId()).isEqualTo(user.getId());
        Assertions.assertThat(chatRoomDetailsDto.getImageUrl()).isEqualTo(DEFAULT_IMG_URL);
        Assertions.assertThat(chatRoomDetailsDto.getMasterUserName()).isEqualTo(user.getUsername());
        Assertions.assertThat(chatRoomDetailsDto.getCurrentMemberCount()).isEqualTo(currentMemberCount);
    }

    @Test
    @DisplayName("채팅방 삭제 테스트")
    public void deleteChatRoom(){
        //given
        Long chatRoomId = 1L;
        Long userId = 1L;

        //when
        doNothing().when(chatRoomRepository).deleteById(anyLong());
        chatRoomService.deleteChatRoom(chatRoomId, userId);

        //then
        verify(chatRoomImageService, times(1)).deleteImageById(chatRoomId);
        verify(messageRepository, times(1)).deleteByChatRoomId(chatRoomId);
        verify(currentPlaylistRepository, times(1)).deleteByChatRoomId(chatRoomId);
        verify(chatRoomRepository).deleteById(chatRoomId);
    }

    @Test
    @DisplayName("방장이 아닌 유저가 채팅방 삭제 요청 시 예외발생")
    void deleteChatRoomOnlyCanMasterUser(){
        // given
        Long chatRoomId = 1L;
        Long masterUserId = 1L;
        Long userId = 2L;
        ChatRoom chatRoom = createChatRoom("room",
                ChatRoomPrivacy.PUBLIC,
                ChatRoomPermission.MASTER,
                S3Directory.CHATROOM.getDefaultImageUrl());
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ReflectionTestUtils.setField(chatRoom, "masterUserId", chatRoomId);
        User user = createUser(masterUserId, "master", DEFAULT_IMG_URL);
        // when
        Assertions.assertThatThrownBy(()->chatRoomService.deleteChatRoom(chatRoomId, userId))
                .isInstanceOf(NotMasterUserException.class);

        // then
    }
    @Test
    @DisplayName("채팅방 입장 테스트")
    public void enterChatRoomTest1(){
        //given
        Long chatRoomId = 1L;
        Long userId = 1L;
        Integer now = 3;
        // 입장할 유저
        User user = new User();
        user.setId(userId);
        // 입장할 채팅방 - 현재 채팅방 인원 수 now
        ChatRoom chatRoom = createChatRoom("name", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", now);

        //when
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(anyLong()))
                .thenReturn(Optional.of(chatRoom));
        // 유저 중복x 설정
        when(userChatRoomRepository.findByUserIdAndChatRoomId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        chatRoomService.enterChatRoom(userId, chatRoomId);

        //then
        // 현재 인원 now 에 한명 추가
        Assertions.assertThat(chatRoom.getCurrentMemberCount()).isEqualTo(now+1);
        // 유저와 연관관계 생겼는지 확인
        Assertions.assertThat(chatRoom.getUserChatRooms().get(0).getUser()).isEqualTo(user);
        Assertions.assertThat(user.getUserChatRoom().get(0).getChatRoom()).isEqualTo(chatRoom);
    }

    @Test
    @DisplayName("채팅방 최대인원을 초과하면 예외 발생 테스트")
    public void chatRoomThrowsExceptionOnCapacityLimitExceeded(){
        //given
        Long chatRoomId = 1L;
        Long userId = 1L;
        Integer now = 100;
        // 입장할 유저
        User user = new User();
        user.setId(userId);
        // 입장할 채팅방 - 현재 채팅방 꽉찬 상태
        ChatRoom chatRoom = createChatRoom("name", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", now);

        //when
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(anyLong()))
                .thenReturn(Optional.of(chatRoom));


        //then
        Assertions.assertThatThrownBy(()->chatRoomService.enterChatRoom(userId, chatRoomId))
                .isInstanceOf(ChatRoomFullException.class)
                .hasMessage("ChatRoom with id: " + chatRoomId + " is full.");
    }

    @Test
    @DisplayName("채팅방 퇴장 시 userChatRoom status : ENTER -> LEAVE 로 업데이트")
    public void leaveChatRoom(){
        //given
        Long chatRoomId = 1L;
        Long userId = 1L;
        int now = 3;
        // 퇴장할 유저
        User user = new User();
        user.setId(userId);
        // 퇴장할 채팅방
        ChatRoom chatRoom = createChatRoom("name", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", now);
        ReflectionTestUtils.setField(chatRoom, "masterUserId", 5L);

        // 유저 - 채팅방의 연관관계 설정 - 현재 입장한 상태
        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);

        //when
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(chatRoomRepository.findById(anyLong()))
                .thenReturn(Optional.of(chatRoom));
        when(userChatRoomRepository.findByUserIdAndChatRoomId(anyLong(), anyLong()))
                .thenReturn(Optional.of(userChatRoom));

        ResponseEntity<?> result = chatRoomService.leaveChatRoom(userId, chatRoomId);

        //then
        Assertions.assertThat(result).isEqualTo(new ResponseEntity<>(HttpStatus.OK));
        Assertions.assertThat(chatRoom.getCurrentMemberCount()).isEqualTo(now-1);
        // 연관관계 상태 업데이트되었는지 확인
        Assertions.assertThat(user.getUserChatRoom().get(0).getStatus()).isEqualTo(UserChatRoomStatus.LEAVE);
    }

    @Test
    @DisplayName("방장인 아닌 유저가 채팅방 퇴장시 채팅방이 삭제되면 안됨")
    void chatRoomNotDeletedWhenRegularUserExits(){
        // given
        // 방장
        Long masterId = 1L;
        User master = createUser(masterId, "master", DEFAULT_IMG_URL);
        // 퇴장할 유저
        Long userId = 2L;
        User user = createUser(userId, "user", DEFAULT_IMG_URL);

        Long chatRoomId = 1L;
        ChatRoom chatRoom = createChatRoom("name", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ReflectionTestUtils.setField(chatRoom, "masterUserId", masterId);
        ReflectionTestUtils.setField(chatRoom, "masterUserName", master.getUsername());
        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", 2);
        UserChatRoom masterUserChatRoom = UserChatRoom.setUserChatRoom(master, chatRoom, UserChatRoomStatus.ENTER);
        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);
        // when
        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)).thenReturn(Optional.of(userChatRoom));

        ResponseEntity<?> result = chatRoomService.leaveChatRoom(userId, chatRoomId);
        // then
        Assertions.assertThat(result).isEqualTo(new ResponseEntity<>(HttpStatus.OK));
        Assertions.assertThat(chatRoom.getCurrentMemberCount()).isEqualTo(1);
        Assertions.assertThat(chatRoom.getUserChatRooms().get(0).getUser().getId()).isEqualTo(masterId);
    }

    @Test
    @DisplayName("방장이 채팅방 퇴장 시 오류 발생")
    void masterUserNeverLeaveChatRoom(){
        // given
        // 방장
        Long masterId = 1L;
        User master = createUser(masterId, "master", DEFAULT_IMG_URL);
        // 퇴장할 유저
        Long userId = 2L;
        User user = createUser(userId, "user", DEFAULT_IMG_URL);

        Long chatRoomId = 1L;
        ChatRoom chatRoom = createChatRoom("name", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ReflectionTestUtils.setField(chatRoom, "masterUserId", masterId);
        ReflectionTestUtils.setField(chatRoom, "masterUserName", master.getUsername());
        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", 2);
        UserChatRoom masterUserChatRoom = UserChatRoom.setUserChatRoom(master, chatRoom, UserChatRoomStatus.ENTER);
        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);
        // when
        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));

        ResponseEntity<?> result = chatRoomService.leaveChatRoom(masterId, chatRoomId);
        // then
        Assertions.assertThat(result).isEqualTo(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("유저 퇴장시 채팅방이 비어있다면 퇴장 시 채팅방 삭제 + 플레이리스트 삭제")
    public void leaveChatRoomTest2(){
        //given
        Long chatRoomId = 1L;
        Long userId = 1L;
        // 퇴장 유저
        User user = new User();
        user.setId(userId);
        // 퇴장할 채팅방
        ChatRoom chatRoom = createChatRoom("name", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", 0);
        ReflectionTestUtils.setField(chatRoom, "masterUserId", 100L);

        // 유저 - 채팅방의 연관관계 설정 - 현재 입장
        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);

        //when
        when(chatRoomRepository.findById(chatRoomId))
                .thenReturn(Optional.of(chatRoom));

        doNothing().when(currentPlaylistRepository).deleteByChatRoomId(chatRoomId);
        doNothing().when(chatRoomRepository).deleteById(chatRoomId);

        ResponseEntity<?> result = chatRoomService.leaveChatRoom(userId, chatRoomId);

        //then
        Assertions.assertThat(result).isEqualTo(new ResponseEntity<>(HttpStatus.OK));
        verify(currentPlaylistRepository, times(1)).deleteByChatRoomId(chatRoomId);
        verify(chatRoomRepository, times(1)).deleteById(chatRoomId);

    }

    @Test
    @DisplayName("채팅방으로 친구 초대 테스트")
    public void inviteFriendsTest(){
        //given
        Long chatRoomId = 1L;
        int newMemberCount = 5;
        int nowMemberCount = 3;
        ChatRoom chatRoom = createChatRoom("room", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.MASTER, DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", nowMemberCount);
        List<Long> friendIds = new ArrayList<>();
        for(Long i=0L;i<newMemberCount;i++){
            friendIds.add(i);
        }

        //when
        when(userRepository.findById(anyLong())).thenAnswer(
                invocation -> {
                    Long userId = invocation.getArgument(0);
                    User user = new User();
                    user.setId(userId);
                    return Optional.of(user);
                }
        );
        when(chatRoomRepository.findById(chatRoom.getId())).thenReturn(Optional.of(chatRoom));
        // 처음 입장하는 상태
        when(userChatRoomRepository.findByUserIdAndChatRoomId(anyLong(), anyLong())).thenReturn(Optional.empty());
        chatRoomService.inviteFriends(chatRoom.getId(), friendIds);

        //then
        Assertions.assertThat(chatRoom.getCurrentMemberCount())
                .isEqualTo(nowMemberCount+newMemberCount);
        for(int i=0;i<newMemberCount;i++){
            Assertions.assertThat(chatRoom.getUserChatRooms().get(i).getUser().getId()).isEqualTo(i);
        }
    }

    public User createUser(Long userId, String username, String imageUrl) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setNickname("nickname");
//        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setEmail("email");
        user.setRole("ROLE_USER");
        user.setImageUrl(imageUrl);
        user.setStatus(UserStatus.PUBLIC);
        return user;
    }

    public Message createMessage(User user, ChatRoom chatRoom, String content, UserChatRoom userChatRoom) {
        return Message.setMessage(content, MessageType.CHAT, userChatRoom, chatRoom.getId(), user.getImageUrl());
    }

    ChatRoom createChatRoom(String name, ChatRoomPrivacy chatRoomPrivacy, ChatRoomPermission permission, String imageUrl){
        return ChatRoom.builder()
                .name(name)
                .privacy(chatRoomPrivacy)
                .imageUrl(imageUrl)
                .permission(permission)
                .build();
    }
}