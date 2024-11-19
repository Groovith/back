package com.groovith.groovith.service;

import com.groovith.groovith.domain.Friend;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.FriendListResponseDto;
import com.groovith.groovith.dto.UserDetailsResponseDto;
import com.groovith.groovith.exception.AlreadyFriendException;
import com.groovith.groovith.exception.FriendNotFoundException;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.FriendRepository;
import com.groovith.groovith.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
@Transactional
@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * 친구 만들기(from_user 의 친구 목록에 to_user 추가)
     */
    public void addFriend(Long fromId, String toUserName) {
        // 친구 추가한 유저
        User fromUser = findUserById(fromId);
        // 친구 추가된 유저
        User toUser = findUserByUsername(toUserName);
        validateFriendExists(fromUser, toUser);
        Friend friend = createFriend(fromUser, toUser);

        friendRepository.save(friend);
    }

    /**
     * 친구 제거하기
     */
    public void subFriend(Long fromId, String toUserName) {
        User fromUser = findUserById(fromId);
        User toUser = findUserByUsername(toUserName);

        Friend friend = validateFriendNotExists(fromUser, toUser);

        friendRepository.deleteById(friend.getId());
    }

    /**
     * 친구 목록 불러오기
     */
    @Transactional(readOnly = true)
    public FriendListResponseDto getFriends(Long userId) {

        User user = findUserById(userId);
        Set<Long> friendsIdsFromUser = new HashSet<>(friendRepository.findFriendsIdsFromUser(user));
        List<User> friendsFromUser = userRepository.findAllById(friendsIdsFromUser);

        return new FriendListResponseDto(friendsFromUser.stream().map(UserDetailsResponseDto::new).toList());
    }

    private Friend createFriend(User fromUser, User toUser) {
        return Friend.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }

    private void validateFriendExists(User fromUser, User toUser) {
        if (friendRepository.existsByFromUserAndToUser(fromUser, toUser)) {
            throw new AlreadyFriendException(fromUser.getId(), toUser.getId());
        }
    }

    private Friend validateFriendNotExists(User fromUser, User toUser) {
        return friendRepository.findByFromUserAndToUser(fromUser, toUser)
                .orElseThrow(() -> new FriendNotFoundException(fromUser.getId(), toUser.getId()));
    }
}
