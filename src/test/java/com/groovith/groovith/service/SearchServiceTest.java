package com.groovith.groovith.service;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.enums.ChatRoomStatus;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @InjectMocks SearchService searchService;
    @Mock ChatRoomRepository chatRoomRepository;
    @Mock UserRepository userRepository;

//    @Test
//    @DisplayName("채팅방 검색 테스트: 비공개 시 검색결과 미포함")
//    public void searchChatRoomsByName() throws Exception{
//        //given
//        String query = "room";
//        ChatRoom chatRoom1 = createChatRoom("room1", ChatRoomStatus.PUBLIC);
//        ChatRoom chatRoom2 = createChatRoom("room2", ChatRoomStatus.PUBLIC);
//        ChatRoom chatRoom3 = createChatRoom("room3", ChatRoomStatus.PRIVATE);
//        ChatRoom chatRoom4 = createChatRoom("방1", ChatRoomStatus.PRIVATE);
//        ChatRoom chatRoom5 = createChatRoom("방2", ChatRoomStatus.PRIVATE);
//        ChatRoomDetailsDto detailsDto1 = new ChatRoomDetailsDto(chatRoom1);
//        ChatRoomDetailsDto detailsDto2 = new ChatRoomDetailsDto(chatRoom2);
//        ChatRoomDetailsDto detailsDto3 = new ChatRoomDetailsDto(chatRoom3);
//
//
//        // chatRoomRepository.findChatRoomByNameContaining(query) 결과
//        List<ChatRoom> data = Arrays.asList(chatRoom1, chatRoom2, chatRoom3, chatRoom4, chatRoom5);
//
//        //when
//        when(chatRoomRepository.findChatRoomByNameContaining(query)).thenReturn(data);
//        SearchChatRoomsResponseDto result = searchService.searchChatRoomsByName(query);
//
//        //then
//        // 검색 시 검색어가 포함되고 status=PUBLIC 인것만 검색결과에 포함
//        Assertions.assertThat(result.getChatRooms().size()).isEqualTo(2);
//        Assertions.assertThat(result.getChatRooms()).containsOnly(detailsDto1, detailsDto2);
//
//    }

    ChatRoom createChatRoom(String name, ChatRoomStatus chatRoomStatus){
        return ChatRoom.builder()
                .name(name)
                .chatRoomStatus(chatRoomStatus)
                .build();
    }
}