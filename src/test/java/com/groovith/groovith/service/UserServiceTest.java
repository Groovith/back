package com.groovith.groovith.service;

import com.groovith.groovith.domain.Follow;
import com.groovith.groovith.domain.enums.FollowStatus;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.repository.FollowRepository;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.security.JwtUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private FollowRepository followRepository;
    @Mock private JwtUtil jwtUtil;


//    @Test
//    public void join(){
//
//        //given
//        String username = "user";
//        String password = "1234";
//
//        JoinDto joinDto = new JoinDto();
//        joinDto.setUsername(username);
//        joinDto.setPassword(password);
//
//        // userRepository.save()가 리턴할 유저
//        String encodedPassword = "encodedPassword";
//
//
//        //when
//        when(userRepository.existsByUsername(username)).thenReturn(false);
//        when(bCryptPasswordEncoder.encode(password)).thenReturn(encodedPassword);
//
//        userService.join(joinDto);
//
//        //then
//        verify(userRepository).save(argThat(data ->
//                        data.getUsername().equals(username) &&
//                        data.getPassword().equals(encodedPassword) &&
//                        data.getRole().equals("ROLE_USER") &&
//                        data.getStreaming() == StreamingType.NONE
//        ));
//    }

//    @Test
//    public void getUser(){
//        //given
//        String accessToken = "token";
//        Long userId = 1L;
//        User user = createUser(userId, "user", "1234");
//
//        when(jwtUtil.getUserId(accessToken)).thenReturn(userId);
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//
//        //when
//        User findUser = userService.getUserByAccessToken(accessToken);
//
//        //then
//        Assertions.assertThat(findUser).isEqualTo(user);
//        Assertions.assertThat(findUser.getUsername()).isEqualTo(user.getUsername());
//    }

//    @Test
//    public void saveSpotifyToken(){
//        //given
//        String accessToken = "access";
//        String refreshToken = "refresh";
//        Long userId = 1L;
//
//        User user = createUser(userId, "user", "1234");
//
//        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
//        when(userRepository.save(any())).thenReturn(any());
//
//        //when
//        userService.saveSpotifyToken(userId, accessToken, refreshToken);
//
//        //then
//        Assertions.assertThat(user.getId()).isEqualTo(userId);
//        Assertions.assertThat(user.getSpotifyAccessToken()).isEqualTo(accessToken);
//        Assertions.assertThat(user.getSpotifyRefreshToken()).isEqualTo(refreshToken);
//        Assertions.assertThat(user.getStreaming()).isEqualTo(StreamingType.SPOTIFY);
//    }

//    @Test
//    public void removeSpotifyTokens(){
//
//        //given
//        Long userId = 1L;
//        User user = createUser(userId, "user", "1234");
//        user.setSpotifyAccessToken("access");
//        user.setSpotifyRefreshToken("refresh");
//        user.setStreaming(StreamingType.SPOTIFY);
//
//        when(userRepository.findById(any())).thenReturn(Optional.of(user));
//        when(userRepository.save(any())).thenReturn(any());
//
//        //when
//        userService.removeSpotifyToken(user.getId());
//
//        //then
//        Assertions.assertThat(user.getId()).isEqualTo(userId);
//        Assertions.assertThat(user.getSpotifyAccessToken()).isEqualTo(null);
//        Assertions.assertThat(user.getSpotifyRefreshToken()).isEqualTo(null);
//        Assertions.assertThat(user.getStreaming()).isEqualTo(StreamingType.NONE);
//
//    }
@Test
public void getUserByUsername_팔로우_관계아닐경우(){
    //given
    Long userId = 1L;
    Long findUserId = 2L;
    // 현재 로그인 중인 유저
    User user = createUser(userId, "user", "1234");
    // 조회하려는 유저
    User findUser = createUser(findUserId, "findUser", "1234");

    //when
    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(findUser));
    // 팔로우 요청 없음
    when(followRepository.findByFollowerIdAndFollowingId(anyLong(), anyLong())).thenReturn(Optional.empty());

    UserDetailsResponseDto result = userService.getUserByUsername(findUser.getUsername(), userId);

    //then
    Assertions.assertThat(result.getUsername()).isEqualTo(findUser.getUsername());
    Assertions.assertThat(result.getStatus()).isEqualTo(FollowStatus.NOFOLLOW);
}

    @Test
    public void getUserByUsername_팔로우_관계인경우(){
        //given
        Long userId = 1L;
        Long findUserId = 2L;
        // 현재 로그인 중인 유저
        User user = createUser(userId, "user", "1234");
        // 조회하려는 유저
        User findUser = createUser(findUserId, "findUser", "1234");

        Follow follow = createFollow(user, findUser, FollowStatus.ACCEPTED);

        //when
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(findUser));
        when(followRepository.findByFollowerIdAndFollowingId(anyLong(), anyLong())).thenReturn(Optional.of(follow));

        UserDetailsResponseDto result = userService.getUserByUsername(findUser.getUsername(), userId);

        //then
        Assertions.assertThat(result.getUsername()).isEqualTo(findUser.getUsername());
        Assertions.assertThat(result.getStatus()).isEqualTo(FollowStatus.ACCEPTED);
    }

    public User createUser(Long id, String username, String password){
        User data = new User();
        data.setId(id);
        data.setUsername(username);
        data.setPassword(password);
        data.setRole("ROLE_USER");
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