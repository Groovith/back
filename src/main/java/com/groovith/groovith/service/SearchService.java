package com.groovith.groovith.service;

import com.groovith.groovith.domain.ChatRoom;
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
            UserDetailsResponseDto dto = new UserDetailsResponseDto(user.getId(), user.getUsername());
            userResponsDtos.add(dto);
        }
        return new SearchUsersResponseDto(userResponsDtos);
    }

    public SearchChatRoomsResponseDto searchChatRoomsByName(String name) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomByNameContaining(name);
        List<ChatRoomDetailsDto> chatRoomDetailsDtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomDetailsDto dto = new ChatRoomDetailsDto(chatRoom);
            chatRoomDetailsDtos.add(dto);
        }
        return new SearchChatRoomsResponseDto(chatRoomDetailsDtos);
    }
}
