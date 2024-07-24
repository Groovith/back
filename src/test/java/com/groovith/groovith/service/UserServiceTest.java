package com.groovith.groovith.service;

import com.groovith.groovith.domain.StreamingType;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.JoinDto;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.security.JwtUtil;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock private JwtUtil jwtUtil;


    @Test
    public void join(){

        //given
        String username = "user";
        String password = "1234";

        JoinDto joinDto = new JoinDto();
        joinDto.setUsername(username);
        joinDto.setPassword(password);

        // userRepository.save()가 리턴할 유저
        String encodedPassword = "encodedPassword";


        //when
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(bCryptPasswordEncoder.encode(password)).thenReturn(encodedPassword);

        userService.join(joinDto);

        //then
        verify(userRepository).save(argThat(data ->
                        data.getUsername().equals(username) &&
                        data.getPassword().equals(encodedPassword) &&
                        data.getRole().equals("ROLE_USER") &&
                        data.getStreaming() == StreamingType.NONE
        ));;
    }

    @Test
    public void getUser(){
        //given
        String accessToken = "token";
        Long userId = 1L;
        User user = createUser(userId, "user", "1234");

        when(jwtUtil.getUserId(accessToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        //when
        User findUser = userService.getUser(accessToken);

        //then
        Assertions.assertThat(findUser).isEqualTo(user);
        Assertions.assertThat(findUser.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void saveSpotifyToken(){
        //given
        String accessToken = "access";
        String refreshToken = "refresh";
        Long userId = 1L;

        User user = createUser(userId, "user", "1234");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(any());

        //when
        userService.saveSpotifyTokens(userId, accessToken, refreshToken);

        //then
        Assertions.assertThat(user.getId()).isEqualTo(userId);
        Assertions.assertThat(user.getSpotifyAccessToken()).isEqualTo(accessToken);
        Assertions.assertThat(user.getSpotifyRefreshToken()).isEqualTo(refreshToken);
        Assertions.assertThat(user.getStreaming()).isEqualTo(StreamingType.SPOTIFY);
    }

    @Test
    public void removeSpotifyTokens(){

        //given
        Long userId = 1L;
        User user = createUser(userId, "user", "1234");
        user.setSpotifyAccessToken("access");
        user.setSpotifyRefreshToken("refresh");
        user.setStreaming(StreamingType.SPOTIFY);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(any());

        //when
        userService.removeSpotifyTokens(user.getId());

        //then
        Assertions.assertThat(user.getId()).isEqualTo(userId);
        Assertions.assertThat(user.getSpotifyAccessToken()).isEqualTo(null);
        Assertions.assertThat(user.getSpotifyRefreshToken()).isEqualTo(null);
        Assertions.assertThat(user.getStreaming()).isEqualTo(StreamingType.NONE);

    }

    public User createUser(Long id, String username, String password){
        User data = new User();
        data.setId(id);
        data.setUsername(username);
        data.setPassword(password);
        data.setRole("ROLE_USER");
        data.setStreaming(StreamingType.NONE);
        return data;
    }
}