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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public SearchUsersResponseDto searchUsersByName(String name) {
        List<User> users = userRepository.findByUsernameContaining(name);
        List<UserDetailsResponseDto> userResponsDtos = new ArrayList<>();
        for (User user : users) {
            UserDetailsResponseDto dto = new UserDetailsResponseDto(user);
            userResponsDtos.add(dto);
        }
        return new SearchUsersResponseDto(userResponsDtos);
    }

    public SearchChatRoomsResponseDto searchChatRoomsByName(String name) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomByNameContaining(name);
        List<ChatRoomDetailsDto> chatRoomDetailsDtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            // 공개 설정된 채팅방만 검색 결과에 포함
            if(chatRoom.getStatus() == ChatRoomStatus.PUBLIC){
                ChatRoomDetailsDto dto = new ChatRoomDetailsDto(chatRoom);
                chatRoomDetailsDtos.add(dto);
            }
        }
        return new SearchChatRoomsResponseDto(chatRoomDetailsDtos);
    }
}
