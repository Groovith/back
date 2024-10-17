//package com.groovith.groovith.service;
//
//import com.fasterxml.jackson.annotation.JsonTypeInfo;
//import com.groovith.groovith.domain.*;
//import com.groovith.groovith.dto.ChatRoomDetailsDto;
//import com.groovith.groovith.dto.ChatRoomListResponseDto;
//import com.groovith.groovith.dto.CreateChatRoomRequestDto;
//import com.groovith.groovith.exception.ChatRoomFullException;
//import com.groovith.groovith.repository.ChatRoomRepository;
//import com.groovith.groovith.repository.CurrentPlaylistRepository;
//import com.groovith.groovith.repository.UserChatRoomRepository;
//import com.groovith.groovith.repository.UserRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ChatRoomServiceTest {
//
//    @InjectMocks private ChatRoomService chatRoomService;
//    @Mock private ChatRoomRepository chatRoomRepository;
//    @Mock private UserRepository userRepository;
//    @Mock private UserChatRoomRepository userChatRoomRepository;
//    @Mock private CurrentPlaylistRepository currentPlaylistRepository;
//
//    @Test
//    @DisplayName("채팅방 생성 테스트")
//    public void create(){
//        //given
//        Long userId = 1L;
//        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto();
//        requestDto.setStatus(ChatRoomStatus.PUBLIC);
//
//        User user = new User();
//        user.setId(userId);
//
//
//        ChatRoom savedChatRoom = requestDto.toEntity();
//        //when
//        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(savedChatRoom);
//        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
//        when(currentPlaylistRepository.save(any())).thenReturn(any());
//
//        ChatRoom chatRoom = chatRoomService.create(userId, requestDto);
//
//        //then
//        Assertions.assertThat(chatRoom).isEqualTo(savedChatRoom);
//        //User - ChatRoom 이 연관관계가 생기는지 테스트
//        Assertions.assertThat(user.getUserChatRoom().get(0).getChatRoom())
//                .isEqualTo(chatRoom);
//    }
//
//    @Test
//    @DisplayName("채팅방 목록 조회 테스트")
//    public void findAllDesc(){
//        //given
//        ChatRoom chatRoom1 = createChatRoom("room1", ChatRoomStatus.PUBLIC);
//        ChatRoom chatRoom2 = createChatRoom("room2", ChatRoomStatus.PUBLIC);
//        ChatRoom chatRoom3 = createChatRoom("room3", ChatRoomStatus.PUBLIC);
//
//        List<ChatRoom> data = new ArrayList<>();
//        data.add(chatRoom1);
//        data.add(chatRoom2);
//        data.add(chatRoom3);
//
//        //when
//        when(chatRoomRepository.findAllDesc()).thenReturn(data);
//        List<ChatRoomListResponseDto> chatRoomList = chatRoomService.findAllDesc();
//
//        //then
//        Assertions.assertThat(chatRoomList.get(0).getChatRoomName()).isEqualTo("room1");
//        Assertions.assertThat(chatRoomList.get(1).getChatRoomName()).isEqualTo("room2");
//        Assertions.assertThat(chatRoomList.get(2).getChatRoomName()).isEqualTo("room3");
//    }
//
//    @Test
//    @DisplayName("채팅방 상세조회 테스트")
//    public void findChatRoomDetail(){
//        //given
//        Long chatroomId = 1L;
//        ChatRoom chatRoom = createChatRoom("room", ChatRoomStatus.PUBLIC);
//        // ReflectionTestUtils 사용하면 필드값 임의로 지정가능
//        ReflectionTestUtils.setField(chatRoom, "id", chatroomId);
//        ChatRoomDetailsDto dto = new ChatRoomDetailsDto(chatRoom);
//        dto.setChatRoomId(chatroomId);
//
//        System.out.println(dto.getChatRoomId());
//        //when
//        when(chatRoomRepository.findById(anyLong()))
//                .thenReturn(Optional.of(chatRoom));
//
//        ChatRoomDetailsDto chatRoomDetailsDto = chatRoomService.findChatRoomDetail(chatroomId);
//
//        //then
//        Assertions.assertThat(chatRoomDetailsDto.getChatRoomId()).isEqualTo(chatroomId);
//        Assertions.assertThat(chatRoomDetailsDto.getName()).isEqualTo("room");
//    }
//
//    @Test
//    @DisplayName("채팅방 삭제 테스트")
//    public void deleteChatRoom(){
//        //given
//        Long id = 1L;
//
//        //when
//        doNothing().when(chatRoomRepository).deleteById(anyLong());
//        chatRoomService.deleteChatRoom(id);
//
//        //then
//        verify(chatRoomRepository).deleteById(id);
//    }
//
//    @Test
//    @DisplayName("채팅방 입장 테스트")
//    public void enterChatRoomTest1(){
//        //given
//        Long chatRoomId = 1L;
//        Long userId = 1L;
//        Integer now = 3;
//        // 입장할 유저
//        User user = new User();
//        user.setId(userId);
//        // 입장할 채팅방 - 현재 채팅방 인원 수 now
//        ChatRoom chatRoom = createChatRoom("name", ChatRoomStatus.PUBLIC);
//        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
//        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", now);
//
//        //when
//        when(userRepository.findById(anyLong()))
//                .thenReturn(Optional.of(user));
//        when(chatRoomRepository.findById(anyLong()))
//                .thenReturn(Optional.of(chatRoom));
//        // 유저 중복x 설정
//        when(userChatRoomRepository.findByUserIdAndChatRoomId(anyLong(), anyLong()))
//                .thenReturn(Optional.empty());
//
//        chatRoomService.enterChatRoom(userId, chatRoomId);
//
//        //then
//        // 현재 인원 now 에 한명 추가
//        Assertions.assertThat(chatRoom.getCurrentMemberCount()).isEqualTo(now+1);
//        // 유저와 연관관계 생겼는지 확인
//        Assertions.assertThat(chatRoom.getUserChatRooms().get(0).getUser()).isEqualTo(user);
//        Assertions.assertThat(user.getUserChatRoom().get(0).getChatRoom()).isEqualTo(chatRoom);
//
//    }
//
//    @Test
//    @DisplayName("채팅방 최대인원 테스트")
//    public void enterChatRoomTest2(){
//        //given
//        Long chatRoomId = 1L;
//        Long userId = 1L;
//        Integer now = 100;
//        // 입장할 유저
//        User user = new User();
//        user.setId(userId);
//        // 입장할 채팅방 - 현재 채팅방 꽉찬 상태
//        ChatRoom chatRoom = createChatRoom("name", ChatRoomStatus.PUBLIC);
//        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
//        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", now);
//
//        //when
//        when(userRepository.findById(anyLong()))
//                .thenReturn(Optional.of(user));
//        when(chatRoomRepository.findById(anyLong()))
//                .thenReturn(Optional.of(chatRoom));
//        // 유저 중복x 설정
//        when(userChatRoomRepository.findByUserIdAndChatRoomId(anyLong(), anyLong()))
//                .thenReturn(Optional.empty());
//
//        //then
//        Assertions.assertThatThrownBy(()->chatRoomService.enterChatRoom(userId, chatRoomId))
//                .isInstanceOf(ChatRoomFullException.class)
//                .hasMessage("ChatRoom with id: " + chatRoomId + " is full.");
//    }
//
//    @Test
//    @DisplayName("채팅방 퇴장 테스트")
//    public void leaveChatRoomTest1(){
//        //given
//        Long chatRoomId = 1L;
//        Long userId = 1L;
//        Integer now = 3;
//        // 퇴장 유저
//        User user = new User();
//        user.setId(userId);
//        // 퇴장할 채팅방
//        ChatRoom chatRoom = createChatRoom("name", ChatRoomStatus.PUBLIC);
//        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
//        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", now);
//
//        // 유저 - 채팅방의 연관관계 설정
//        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom);
//
//        //when
//        when(userRepository.findById(anyLong()))
//                .thenReturn(Optional.of(user));
//        when(chatRoomRepository.findById(anyLong()))
//                .thenReturn(Optional.of(chatRoom));
//        when(userChatRoomRepository.findByUserIdAndChatRoomId(anyLong(), anyLong()))
//                .thenReturn(Optional.of(userChatRoom));
//
//        ChatRoomMemberStatus result = chatRoomService.leaveChatRoom(userId, chatRoomId);
//
//        //then
//        Assertions.assertThat(result).isEqualTo(ChatRoomMemberStatus.ACTIVE);
//        Assertions.assertThat(chatRoom.getCurrentMemberCount()).isEqualTo(now-1);
//        // 연관관계 없어졌는지 테스트
//        Assertions.assertThat(chatRoom.getUserChatRooms().size()).isEqualTo(0);
//        Assertions.assertThat(user.getUserChatRoom().size()).isEqualTo(0);
//    }
//
//
//    @Test
//    @DisplayName("유저 퇴장시 채팅방이 비어있다면 채팅방 삭제 테스트")
//    public void leaveChatRoomTest2(){
//        //given
//        Long chatRoomId = 1L;
//        Long userId = 1L;
//        Integer now = 1;
//        // 퇴장 유저
//        User user = new User();
//        user.setId(userId);
//        // 퇴장할 채팅방
//        ChatRoom chatRoom = createChatRoom("name", ChatRoomStatus.PUBLIC);
//        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
//        ReflectionTestUtils.setField(chatRoom, "currentMemberCount", now);
//
//        // 유저 - 채팅방의 연관관계 설정
//        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom);
//
//        //when
//        when(userRepository.findById(anyLong()))
//                .thenReturn(Optional.of(user));
//        when(chatRoomRepository.findById(anyLong()))
//                .thenReturn(Optional.of(chatRoom));
//        when(userChatRoomRepository.findByUserIdAndChatRoomId(anyLong(), anyLong()))
//                .thenReturn(Optional.of(userChatRoom));
//        doNothing().when(currentPlaylistRepository).deleteByChatRoomId(anyLong());
//
//        ChatRoomMemberStatus result = chatRoomService.leaveChatRoom(userId, chatRoomId);
//
//        //then
//        verify(currentPlaylistRepository).deleteByChatRoomId(chatRoomId);
//
//        // EMPTY 상태인지
//        Assertions.assertThat(result).isEqualTo(ChatRoomMemberStatus.EMPTY);
//
//        // 연관관계 없어졌는지 테스트
//        Assertions.assertThat(chatRoom.getUserChatRooms().size()).isEqualTo(0);
//        Assertions.assertThat(user.getUserChatRoom().size()).isEqualTo(0);
//
//    }
//
//    ChatRoom createChatRoom(String name, ChatRoomStatus chatRoomStatus){
//        return ChatRoom.builder()
//                .name(name)
//                .chatRoomStatus(chatRoomStatus)
//                .build();
//    }
//}