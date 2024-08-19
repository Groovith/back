package com.groovith.groovith.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.groovith.groovith.domain.StreamingType;
import com.groovith.groovith.dto.EmailCheckResponseDto;
import com.groovith.groovith.dto.SpotifyTokenResponseDto;
import com.groovith.groovith.dto.UserDetailsResponseDto;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.JoinDto;
import com.groovith.groovith.security.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private String DEFAULT_IMG_URL = "https://groovith-bucket.s3.ap-northeast-2.amazonaws.com/user/user_default.png";

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
        data.setImageUrl(DEFAULT_IMG_URL);

        userRepository.save(data);
    }

    /**
     * Access Token 을 사용해 User 객체 반환
     *
     * @param accessToken 서버 Access Token
     * @return 찾은 User 객체 | User 가 DB에 없으면 UserNotFoundException 발생
     */
    public User getUserByAccessToken(String accessToken) {

        Long userId = jwtUtil.getUserId(accessToken);

        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public UserDetailsResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        return new UserDetailsResponseDto(user);
    }

    /**
     * Spotify 인증 후 발급 받은 토큰 저장
     *
     * @param userId       User ID
     * @param refreshToken Spotify Refresh Token (없을 경우 저장하지 않음)
     */
    public void saveSpotifyToken(Long userId, String refreshToken) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.setSpotifyRefreshToken(refreshToken);
        user.setStreaming(StreamingType.SPOTIFY);
        userRepository.save(user);
    }

    /**
     * Spotify 토큰 삭제 및 연결 해제
     *
     * @param userId User Id
     */
    public void removeSpotifyToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
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
    public SpotifyTokenResponseDto getSpotifyToken(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        SpotifyTokenResponseDto responseDto = new SpotifyTokenResponseDto();
        responseDto.setSpotifyAccessToken(user.getSpotifyRefreshToken());

        return responseDto;
    }

    // 이메일 중복 검사
    public ResponseEntity<EmailCheckResponseDto> checkEmail(String email) {
        try {
            boolean existsByEmail = userRepository.existsByEmail(email);
            if (!existsByEmail) {
                return EmailCheckResponseDto.success();
            } else {
                return EmailCheckResponseDto.duplicateId();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return EmailCheckResponseDto.databaseError();
        }
    }
}
