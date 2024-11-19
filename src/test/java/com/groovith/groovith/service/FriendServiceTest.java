package com.groovith.groovith.service;

import com.groovith.groovith.domain.Friend;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.enums.UserStatus;
import com.groovith.groovith.dto.FriendListResponseDto;
import com.groovith.groovith.exception.AlreadyFriendException;
import com.groovith.groovith.exception.FriendNotFoundException;
import com.groovith.groovith.repository.FriendRepository;
import com.groovith.groovith.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Array;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @InjectMocks FriendService friendService;
    @Mock FriendRepository friendRepository;
    @Mock UserRepository userRepository;

    @Test
    @DisplayName("친구 만들기 테스트")
    void addFriend(){
        // given
        Long fromId = 1L;
        Long toId = 2L;
        User fromUser = createUser(fromId, "fromUser");
        User toUser = createUser(toId, "toUser");

        // when
        when(userRepository.findById(fromId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findByUsername(toUser.getUsername())).thenReturn(Optional.of(toUser));
        when(friendRepository.existsByFromUserAndToUser(fromUser, toUser)).thenReturn(false);

        friendService.addFriend(fromId, toUser.getUsername());
        // then
        verify(friendRepository, times(1)).save(any(Friend.class));
        Assertions.assertThat(fromUser.getFriends().get(0).getToUser()).isEqualTo(toUser);
    }

    @Test
    @DisplayName("친구 추가시 이미 친구인 경우 예외 발생")
    void alreadyFriendException(){
        // given
        Long fromId = 1L;
        Long toId = 2L;
        User fromUser = createUser(fromId, "fromUser");
        User toUser = createUser(toId, "toUser");

        // when
        when(userRepository.findById(fromId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findByUsername(toUser.getUsername())).thenReturn(Optional.of(toUser));
        when(friendRepository.existsByFromUserAndToUser(fromUser, toUser)).thenReturn(true);

        // then
        Assertions.assertThatThrownBy(()->friendService.addFriend(fromId, toUser.getUsername()))
                .isInstanceOf(AlreadyFriendException.class)
                .hasMessage("이미 친구관계입니다. from:"+fromId+" to:"+ toId);
    }

    @Test
    @DisplayName("친구 삭제 테스트")
    void subFriendTest(){
        // given
        Long fromId = 1L;
        Long toId = 2L;
        Long friendId = 3L;
        User fromUser = createUser(fromId, "fromUser");
        User toUser = createUser(toId, "toUser");
        Friend friend = Friend.setFriend(fromUser, toUser);
        ReflectionTestUtils.setField(friend, "id", friendId);
        // when
        when(userRepository.findById(fromId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findByUsername(toUser.getUsername())).thenReturn(Optional.of(toUser));
        when(friendRepository.findByFromUserAndToUser(fromUser, toUser)).thenReturn(Optional.of(friend));
        friendService.subFriend(fromId, toUser.getUsername());

        // then
        verify(friendRepository, times(1)).deleteById(friend.getId());
        Assertions.assertThat(fromUser.getFriends().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("친구 삭제하려는데 친구가 아닌경우 예외발생")
    void notFoundFriend(){
        // given
        Long fromId = 1L;
        Long toId = 2L;
        Long friendId = 3L;
        User fromUser = createUser(fromId, "fromUser");
        User toUser = createUser(toId, "toUser");
        // when
        when(userRepository.findById(fromId)).thenReturn(Optional.of(fromUser));
        when(userRepository.findByUsername(toUser.getUsername())).thenReturn(Optional.of(toUser));
        when(friendRepository.findByFromUserAndToUser(fromUser, toUser)).thenReturn(Optional.empty());
        // then
        Assertions.assertThatThrownBy(()->friendService.subFriend(fromId, toUser.getUsername()))
                .isInstanceOf(FriendNotFoundException.class)
                .hasMessage("친구 관계가 아닙니다. from: "+fromId+" to: "+toId);
    }

    @Test
    @DisplayName("친구 목록 불러오기 테스트")
    void getFriends(){
        // given
        Long userId = 100L;
        User user = createUser(userId, "user");
        User user1 = createUser(1L, "user1");
        User user2 = createUser(2L, "user2");
        User user3 = createUser(3L, "user3");
        Set<Long> friendsIds = new HashSet<>();
        friendsIds.add(1L);
        friendsIds.add(2L);
        friendsIds.add(3L);
        List<Long> friendsIdsList = Arrays.asList(1L, 2L, 3L);
        List<User> friends = Arrays.asList(user1, user2, user3);
        // when
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(friendRepository.findFriendsIdsFromUser(user)).thenReturn(friendsIdsList);
        when(userRepository.findAllById(friendsIds)).thenReturn(friends);
        FriendListResponseDto friendListResponseDto = friendService.getFriends(userId);

        // then
        Assertions.assertThat(friendListResponseDto.getFriends().size()).isEqualTo(3);
        Assertions.assertThat(friendListResponseDto.getFriends().get(0).getId()).isEqualTo(user1.getId());
        Assertions.assertThat(friendListResponseDto.getFriends().get(1).getId()).isEqualTo(user2.getId());
        Assertions.assertThat(friendListResponseDto.getFriends().get(2).getId()).isEqualTo(user3.getId());
    }


    private Friend createFriend(User fromUser, User toUser) {
        return Friend.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();
    }

    public User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setNickname("nickname");
//        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setEmail("email");
        user.setRole("ROLE_USER");
        user.setImageUrl("img");
        user.setStatus(UserStatus.PUBLIC);
        return user;
    }
}