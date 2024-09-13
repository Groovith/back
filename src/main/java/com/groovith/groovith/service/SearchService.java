package com.groovith.groovith.service;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomStatus;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.ChatRoomDetailsDto;
import com.groovith.groovith.dto.SearchChatRoomsResponseDto;
import com.groovith.groovith.dto.SearchUsersResponseDto;
import com.groovith.groovith.dto.UserDetailsResponseDto;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public SearchUsersResponseDto searchUsersByName(String name, Pageable pageable, Long lastUserId) {
        Slice<User> users = userRepository.findByUsernameContaining(name, pageable, lastUserId);
//        List<UserDetailsResponseDto> userResponsDtos = new ArrayList<>();
//        for (User user : users) {
//            UserDetailsResponseDto dto = new UserDetailsResponseDto(user);
//            userResponsDtos.add(dto);
//        }
        List<UserDetailsResponseDto> userResponsDtos = users.stream()
                .map(UserDetailsResponseDto::new)
                .toList();
        return new SearchUsersResponseDto(userResponsDtos);
    }

    public SearchChatRoomsResponseDto searchChatRoomsByName(String name, Pageable pageable, Long lastChatRoomId) {
        Slice<ChatRoom> chatRooms = chatRoomRepository.findChatRoomByNameContaining(name, pageable, lastChatRoomId);
        List<ChatRoomDetailsDto> chatRoomDetailsDtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            System.out.println(chatRoom.getName());
            // 공개 설정된 채팅방만 검색 결과에 포함(오픈채팅방)
            if(chatRoom.getStatus() == ChatRoomStatus.PUBLIC){
                ChatRoomDetailsDto dto = new ChatRoomDetailsDto(chatRoom);
                chatRoomDetailsDtos.add(dto);
            }
        }
//        List<ChatRoomDetailsDto> chatRoomDetailsDtos = chatRooms.stream()
//                .filter(chatRoom -> chatRoom.getStatus()==ChatRoomStatus.PUBLIC)    // 오픈 채팅방일 경우만 검색 결과에 포함
//                .map(ChatRoomDetailsDto::new)
//                .toList();
        return new SearchChatRoomsResponseDto(chatRoomDetailsDtos);
    }
}
