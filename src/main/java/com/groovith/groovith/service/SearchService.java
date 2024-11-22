package com.groovith.groovith.service;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.enums.UserRelationship;
import com.groovith.groovith.dto.ChatRoomDetailsDto;
import com.groovith.groovith.dto.SearchChatRoomsResponseDto;
import com.groovith.groovith.dto.SearchUsersResponseDto;
import com.groovith.groovith.dto.UserDetailsResponseDto;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.FriendRepository;
import com.groovith.groovith.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FriendRepository friendRepository;

    public SearchUsersResponseDto searchUsers(Long userId, String name, Pageable pageable, Long lastUserId) {
        Slice<User> findUsers = userRepository.searchUser(name, pageable, lastUserId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Set<Long> friendIdsFromUser = new HashSet<>(friendRepository.findFriendsIdsFromUser(user));

        return new SearchUsersResponseDto(findUsers.stream()
                .map(findUser -> new UserDetailsResponseDto(
                        findUser,
                        getUserRelationship(friendIdsFromUser, user, findUser)))
                .toList());
    }

    public SearchChatRoomsResponseDto searchChatRooms(String name, Pageable pageable, Long lastChatRoomId,Long userId) {
        Slice<ChatRoom> chatRooms = chatRoomRepository.searchChatRoom(name, pageable, lastChatRoomId);

        return new SearchChatRoomsResponseDto(chatRooms.stream().map(chatRoom -> {
            return new ChatRoomDetailsDto(chatRoom, chatRoom.getIsMaster(userId));
        }).toList());
    }

    private UserRelationship getUserRelationship(Set<Long> friendsIdsFromUser, User user, User findUser) {
        if (friendsIdsFromUser.contains(findUser.getId())) {
            return UserRelationship.FRIEND;
        }
        if (user.equals(findUser)) {
            return UserRelationship.SELF;
        }
        return UserRelationship.NOT_FRIEND;
    }
}
