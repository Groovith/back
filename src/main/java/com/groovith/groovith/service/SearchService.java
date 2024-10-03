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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public SearchUsersResponseDto searchUsers(String name, Pageable pageable, Long lastUserId) {
        Slice<User> users = userRepository.searchUser(name, pageable, lastUserId);

        return new SearchUsersResponseDto(users.stream().map(UserDetailsResponseDto::new).toList());
    }

    public SearchChatRoomsResponseDto searchChatRooms(String name, Pageable pageable, Long lastChatRoomId) {
        Slice<ChatRoom> chatRooms = chatRoomRepository.searchChatRoom(name, pageable, lastChatRoomId);

        return new SearchChatRoomsResponseDto(chatRooms.stream().map(ChatRoomDetailsDto::new).toList());
    }
}
