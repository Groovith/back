package com.groovith.groovith.service;

import com.groovith.groovith.domain.StreamingType;
import com.groovith.groovith.dto.SpotifyTokenResponseDto;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.JoinDto;
import com.groovith.groovith.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    public void join(JoinDto joinDto) throws IllegalArgumentException {
        String username = joinDto.getUsername();
        String password = joinDto.getPassword();

        Boolean isExist = userRepository.existsByUsername(username);

        if (isExist) {
            throw new IllegalArgumentException("User with given username already exists");
        }

        User data = new User();

        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole("ROLE_USER");
        data.setStreaming(StreamingType.NONE);

        userRepository.save(data);
    }

    /**
     * Access Token 을 사용해 User 객체 반환
     *
     * @param accessToken 서버 Access Token
     * @return 찾은 User 객체 | User 가 DB에 없으면 UserNotFoundException 발생
     */
    public User getUser(String accessToken) {

        Long userId = jwtUtil.getUserId(accessToken);

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Spotify 인증 후 발급 받은 토큰 저장
     *
     * @param userId       User ID
     * @param accessToken  Spotify Access Token
     * @param refreshToken Spotify Refresh Token (없을 경우 저장하지 않음)
     */
    public void saveSpotifyTokens(Long userId, String accessToken, String refreshToken) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.setSpotifyAccessToken(accessToken);
        if (!refreshToken.equals("")) {
            user.setSpotifyRefreshToken(refreshToken);
        }
        user.setStreaming(StreamingType.SPOTIFY);
        userRepository.save(user);
    }

    /**
     * Spotify 토큰 삭제 및 연결 해제
     *
     * @param userId User Id
     */
    public void removeSpotifyTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setSpotifyAccessToken(null);
        user.setSpotifyRefreshToken(null);
        user.setStreaming(StreamingType.NONE);
        userRepository.save(user);
    }

    /**
     * Spotify 토큰 가져오기
     *
     * @param userId User Id
     * @return SpotifyTokensResponseDto
     */
    public SpotifyTokenResponseDto getSpotifyTokens(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        SpotifyTokenResponseDto responseDto = new SpotifyTokenResponseDto();
        responseDto.setSpotifyToken(user.getSpotifyAccessToken());

        return responseDto;
    }
}
