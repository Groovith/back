package com.groovith.groovith.service;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.ChatRoomDetailDto;
import com.groovith.groovith.dto.SearchChatRoomsResponseDto;
import com.groovith.groovith.dto.SearchUsersResponseDto;
import com.groovith.groovith.dto.UserDetailsResponse;
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
        List<UserDetailsResponse> userDetailsResponses = new ArrayList<>();
        for (User user : users) {
            UserDetailsResponse dto = new UserDetailsResponse(user.getId(), user.getUsername());
            userDetailsResponses.add(dto);
        }
        return new SearchUsersResponseDto(userDetailsResponses);
    }

    public SearchChatRoomsResponseDto searchChatRoomsByName(String name) {
        List<ChatRoom> chatRooms = chatRoomRepository.findChatRoomByNameContaining(name);
        List<ChatRoomDetailDto> chatRoomDetailDtos = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomDetailDto dto = new ChatRoomDetailDto(chatRoom);
            chatRoomDetailDtos.add(dto);
        }
        return new SearchChatRoomsResponseDto(chatRoomDetailDtos);
    }
}
