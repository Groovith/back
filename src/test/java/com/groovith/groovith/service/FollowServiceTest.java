package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.FollowStatus;
import com.groovith.groovith.domain.enums.UserStatus;
import com.groovith.groovith.dto.FollowResponse;
import com.groovith.groovith.dto.UserDetailsResponseDto;
import com.groovith.groovith.repository.FollowRepository;
import com.groovith.groovith.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @InjectMocks private FollowService followService;
    @Mock private FollowRepository followRepository;
    @Mock private UserRepository userRepository;

    @Test
    @DisplayName("PUBLIC 유저에게 팔로우 요청")
    public void follow_to_public(){
        //given
        User follower = createUser(1L, "follower", "1234", UserStatus.PUBLIC);
        User following = createUser(2L, "following", "1234", UserStatus.PUBLIC);

        //when
        when(userRepository.findByUsername(follower.getUsername())).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername(following.getUsername())).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).thenReturn(false);

        ResponseEntity<?> response = followService.follow(follower.getUsername(), following.getUsername());

        //then
        verify(followRepository).save(argThat(data ->
                data.getFollower().equals(follower) &&
                        data.getFollowing().equals(following) &&
                        data.getStatus().equals(FollowStatus.ACCEPTED)

        ));

    }

    @Test
    @DisplayName("PRIVATE 유저에게 팔로우 요청")
    public void follow_to_private(){
        //given
        //팔로워
        User follower = createUser(1L, "follower", "1234", UserStatus.PUBLIC);
        //팔로잉
        User following = createUser(2L, "following", "1234", UserStatus.PRIVATE);

        //when
        when(userRepository.findByUsername(follower.getUsername())).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername(following.getUsername())).thenReturn(Optional.of(following));
        when(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).thenReturn(false);

        ResponseEntity<?> response = followService.follow(follower.getUsername(), following.getUsername());
        //then
        verify(followRepository).save(argThat(data ->
                data.getFollower().equals(follower) &&
                        data.getFollowing().equals(following) &&
                        data.getStatus().equals(FollowStatus.PENDING)
        ));

    }

    @Test
    public void getFollowing(){
        // user 의 팔로잉 목록 가져오기 테스트
        //given
        User user = createUser(1L, "user", "1234", UserStatus.PUBLIC);
        User user1 = createUser(2L, "user1", "1234", UserStatus.PUBLIC);
        User user2 = createUser(3L, "user2", "1234", UserStatus.PUBLIC);
        User user3 = createUser(4L, "user3", "1234", UserStatus.PUBLIC);

        // user 가 user1, user2 팔로우
        // createFollow(following, follower, status)
        Follow follow1 = createFollow(user1, user, FollowStatus.ACCEPTED);
        Follow follow2 = createFollow(user2, user, FollowStatus.ACCEPTED);

        user.getFollowing().add(follow1);
        user.getFollowing().add(follow2);
        user1.getFollowers().add(follow1);
        user2.getFollowers().add(follow2);

        //when
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        FollowResponse followResponse = followService.getFollowing(user.getId());

        //then
        System.out.println(followResponse.getFollowing().stream().map(UserDetailsResponseDto::getUsername).collect(Collectors.toList()));
        Assertions.assertThat(followResponse.getFollowing().size()).isEqualTo(2);

        // user의 팔로잉 목록에 user1, user2 이 포함되야함
        Assertions.assertThat(followResponse.getFollowing()
                        .stream().map(UserDetailsResponseDto::getUsername).toList())
                .containsExactly(user1.getUsername(), user2.getUsername());

        Assertions.assertThat(followResponse.getFollowing()
                .stream().map(UserDetailsResponseDto::getUsername).toList()
                        .contains(user3.getUsername()))
                .isEqualTo(false);

    }

    @Test
    public void getFollowers(){
        // user의 팔로워 목록 가져오기 테스트
        //given
        User user = createUser(1L, "user", "1234", UserStatus.PUBLIC);
        User user1 = createUser(2L, "user1", "1234", UserStatus.PUBLIC);
        User user2 = createUser(3L, "user2", "1234", UserStatus.PUBLIC);
        User user3 = createUser(4L, "user3", "1234", UserStatus.PUBLIC);

        // user1, user2 가 user 팔로우
        // createFollow(following, follower, status)
        Follow follow1 = createFollow(user, user1, FollowStatus.ACCEPTED);
        Follow follow2 = createFollow(user, user2, FollowStatus.ACCEPTED);

        user.getFollowers().add(follow1);
        user.getFollowers().add(follow2);
        user1.getFollowing().add(follow1);
        user2.getFollowing().add(follow2);

        //when
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        FollowResponse followResponse = followService.getFollowers(user.getId());

        //then
        System.out.println(followResponse.getFollower().stream().map(UserDetailsResponseDto::getUsername).collect(Collectors.toList()));
        Assertions.assertThat(followResponse.getFollower().size()).isEqualTo(2);

        // user의 팔로워 목록에 user1, user2 이 포함되야함
        Assertions.assertThat(followResponse.getFollower()
                        .stream().map(UserDetailsResponseDto::getUsername).toList())
                .containsExactly(user1.getUsername(), user2.getUsername());


        Assertions.assertThat(followResponse.getFollower()
                        .stream().map(UserDetailsResponseDto::getUsername).toList()
                        .contains(user3.getUsername()))
                .isEqualTo(false);

    }

    @Test
    public void unfollow(){
        //given
        Long followId = 1L;
        User follower = createUser(1L, "follower", "1234", UserStatus.PUBLIC);
        User following = createUser(2L, "following", "1234", UserStatus.PUBLIC);
        Follow follow = createFollow(following, follower, FollowStatus.ACCEPTED);
        follow.setId(followId);


        //when
        when(userRepository.findByUsername(follower.getUsername())).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername(following.getUsername())).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId()))
                .thenReturn(Optional.of(follow));

        followService.unfollow(follower.getUsername(), following.getUsername());

        //then
        verify(followRepository).deleteById(followId);
    }





    public User createUser(Long id, String username, String password, UserStatus status){
        User data = new User();
        data.setId(id);
        data.setUsername(username);
        data.setPassword(password);
        data.setRole("ROLE_USER");
        data.setStatus(status);
        return data;
    }

    public Follow createFollow(User following, User follower, FollowStatus status){
        Follow follow = new Follow();
        follow.setFollowing(following);
        follow.setFollower(follower);
        follow.setStatus(status);

        return follow;
    }
}