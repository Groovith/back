//package com.groovith.groovith.service;
//
//import com.groovith.groovith.domain.Follow;
//import com.groovith.groovith.domain.StreamingType;
//import com.groovith.groovith.domain.User;
//import com.groovith.groovith.dto.FollowResponse;
//import com.groovith.groovith.dto.UserDetailsResponseDto;
//import com.groovith.groovith.repository.FollowRepository;
//import com.groovith.groovith.repository.UserRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.ResponseEntity;
//
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.argThat;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class FollowServiceTest {
//
//    @InjectMocks private FollowService followService;
//    @Mock private FollowRepository followRepository;
//    @Mock private UserRepository userRepository;
//
//    @Test
//    public void follow(){
//        //given
//        User follower = createUser(1L, "follower", "1234");
//        User following = createUser(2L, "following", "1234");
//
//        Follow follow = new Follow();
//        follow.setFollower(follower);
//        follow.setFollowing(following);
//
//        //when
//        when(userRepository.findByUsername(follower.getUsername())).thenReturn(Optional.of(follower));
//        when(userRepository.findByUsername(following.getUsername())).thenReturn(Optional.of(following));
//        when(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).thenReturn(false);
//
//        ResponseEntity<?> response =followService.follow(follower.getUsername(), following.getUsername());
//
//        //then
//        verify(followRepository).save(argThat(data ->
//                data.getFollower().equals(follower) &&
//                        data.getFollowing().equals(following)
//        ));
//    }
//
//    @Test
//    public void getFollowing(){
//        // user 의 팔로잉 목록 가져오기 테스트
//        //given
//        User user = createUser(1L, "user", "1234");
//        User user1 = createUser(2L, "user1", "1234");
//        User user2 = createUser(3L, "user2", "1234");
//        User user3 = createUser(4L, "user3", "1234");
//
//        Follow follow1 = createFollow(user1, user);
//        Follow follow2 = createFollow(user2, user);
//
//        user.getFollowing().add(follow1);
//        user.getFollowing().add(follow2);
//        user1.getFollowers().add(follow1);
//        user2.getFollowers().add(follow2);
//
//        //when
//        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
//        FollowResponse followResponse = followService.getFollowing(user.getUsername());
//
//        //then
//        Assertions.assertThat(followResponse.getFollowing().size()).isEqualTo(2);
//        Assertions.assertThat(followResponse.getFollowing()
//                .stream().map(UserDetailsResponseDto::getUsername).toList()
//                        .contains(user3.getUsername()))
//                .isEqualTo(false);
//
//    }
//
//    @Test
//    public void getFollowers(){
//        // user의 팔로워 목록 가져오기 테스트
//        //given
//        User user = createUser(1L, "user", "1234");
//        User user1 = createUser(2L, "user1", "1234");
//        User user2 = createUser(3L, "user2", "1234");
//        User user3 = createUser(4L, "user3", "1234");
//
//        Follow follow1 = createFollow(user, user1);
//        Follow follow2 = createFollow(user, user2);
//
//        user.getFollowers().add(follow1);
//        user.getFollowers().add(follow2);
//        user1.getFollowing().add(follow1);
//        user2.getFollowing().add(follow2);
//
//        //when
//        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
//        FollowResponse followResponse = followService.getFollowers(user.getUsername());
//
//        //then
//        Assertions.assertThat(followResponse.getFollower().size()).isEqualTo(2);
//        Assertions.assertThat(followResponse.getFollower()
//                        .stream().map(UserDetailsResponseDto::getUsername).toList()
//                        .contains(user3.getUsername()))
//                .isEqualTo(false);
//
//    }
//
//    @Test
//    public void unfollow(){
//        //given
//        User follower = createUser(1L, "follower", "1234");
//        User following = createUser(2L, "following", "1234");
//        Follow follow = createFollow(following, follower);
//
//
//        //when
//        when(userRepository.findByUsername(follower.getUsername())).thenReturn(Optional.of(follower));
//        when(userRepository.findByUsername(following.getUsername())).thenReturn(Optional.of(following));
//        when(followRepository.existsByFollowerAndFollowing(any(User.class), any(User.class))).thenReturn(true);
//
//        followService.unfollow(follower.getUsername(), following.getUsername());
//
//        //then
//        verify(followRepository).deleteByFollowerAndFollowing(follower, following);
//    }
//
//    public User createUser(Long id, String username, String password){
//        User data = new User();
//        data.setId(id);
//        data.setUsername(username);
//        data.setPassword(password);
//        data.setRole("ROLE_USER");
//        data.setStreaming(StreamingType.NONE);
//        return data;
//    }
//
//    public Follow createFollow(User following, User follower){
//        Follow follow = new Follow();
//        follow.setFollowing(following);
//        follow.setFollower(follower);
//        return follow;
//    }
//}