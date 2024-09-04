package com.groovith.groovith.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.groovith.groovith.domain.*;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.provider.EmailProvider;
import com.groovith.groovith.repository.CertificationRepository;
import com.groovith.groovith.repository.FollowRepository;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailProvider emailProvider;
    private final AmazonS3Client amazonS3Client;
    private final FollowRepository followRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.defaultUserImageUrl}")
    private String DEFAULT_IMG_URL;

    // 회원가입
    public ResponseEntity<JoinResponseDto> join(JoinRequestDto joinRequestDto) {

        try {
            String username = joinRequestDto.getUsername();
            String password = joinRequestDto.getPassword();
            String email = joinRequestDto.getEmail();

            // 동일한 유저네임, 이메일 확인
            boolean isUsernameExist = userRepository.existsByUsername(username);
            boolean isEmailExist = userRepository.existsByEmail(email);
            if (isUsernameExist || isEmailExist) return JoinResponseDto.duplicateId();

            // 이메일 인증 여부 확인
            Certification certification = certificationRepository.findByEmail(email).orElse(null);
            if (certification == null || !certification.isCertificated()) return JoinResponseDto.certificationFail();

            // 새 유저 생성
            User user = new User();
            user.setUsername(username);
            user.setNickname(username);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            user.setEmail(email);
            user.setRole("ROLE_USER");
            user.setStreaming(StreamingType.NONE);
            user.setImageUrl(DEFAULT_IMG_URL);
            user.setStatus(UserStatus.PUBLIC);

            // 유저 저장
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            return JoinResponseDto.databaseError();
        }

        return JoinResponseDto.success();
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

    public UserDetailsResponseDto getUserByUsername(String username, Long userId) {
        // 현재 로그인중인 유저
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        // 조회하려는 유저
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        UserDetailsResponseDto userDetailsResponseDto = new UserDetailsResponseDto(findUser);

        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowingId(userId, findUser.getId());
        // 팔로우 관계가 있을시에 값 업데이트, 기본은 NOFOLLOW
        follow.ifPresent(value -> userDetailsResponseDto.setStatus(value.getStatus()));
        return userDetailsResponseDto;
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

    /**
     * 유저 status 변경
     * */
    @Transactional
    public void updateStatus(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.updateStatus(user.getStatus());
    }

    // 이메일 중복 검사
    public ResponseEntity<CheckEmailResponseDto> checkEmail(String email) {
        try {
            boolean existsByEmail = userRepository.existsByEmail(email);
            if (!existsByEmail) {
                return CheckEmailResponseDto.success();
            } else {
                return CheckEmailResponseDto.duplicateId();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CheckEmailResponseDto.databaseError();
        }
    }

    // 이메일 인증 번호 요청
    public ResponseEntity<EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto requestDto) {
        try {
            String email = requestDto.getEmail();
            // 같은 이메일로 중복 회원 있는지 검증
            boolean isExistEmail = userRepository.existsByEmail(email);
            if (isExistEmail) return EmailCertificationResponseDto.duplicateId();

            String certificationNumber = getCertificationNumber();

            // 이메일 전송 과정 오류 검증
            boolean isSucceed = emailProvider.sendCertificationMail(email, certificationNumber);
            if (!isSucceed) return EmailCertificationResponseDto.mailSendFail();

            // 해당 이메일에 대해 인증 번호 DB 저장
            Certification certification = new Certification(email, certificationNumber, false);
            certificationRepository.save(certification);
        } catch (Exception e) {
            e.printStackTrace();
            return EmailCertificationResponseDto.mailSendFail();
        }

        return EmailCertificationResponseDto.success();
    }

    // 랜덤 네 자리 숫자 문자열 반환
    private static String getCertificationNumber () {
        StringBuilder certificationNumber = new StringBuilder();
        for (int count = 0; count < 4; count++) certificationNumber.append((int) (Math.random() * 10));
        return certificationNumber.toString();
    }

    // 이메일 인증 번호 확인
    public ResponseEntity<CheckCertificationResponseDto> checkCertification(CheckCertificationRequestDto requestDto) {
        try {
            String email = requestDto.getEmail();
            String certificationNumber = requestDto.getCertificationNumber();

            // DB 에서 인증 객체 조회
            Certification certification = certificationRepository.findByEmail(email).orElse(null);
            if (certification == null) return CheckCertificationResponseDto.certificationFail();

            // 이메일과 인증 번호 유효 여부 조회
            boolean isMatched = certification.getEmail().equals(email) && certification.getCertificationNumber().equals(certificationNumber);
            if (!isMatched) return CheckCertificationResponseDto.certificationFail();

            // 유효한 이메일 인증 처리 -> 추후 회원가입에서 이메일 인증 여부 검사
            // memo: 유저네임 및 이메일 중복 여부 확인 -> 이메일 인증 번호 요청 -> 이메일 인증 번호 확인 -> 회원가입 요청 순서
            certification.setCertificated(true);
            certificationRepository.save(certification);
        } catch (Exception e) {
            e.printStackTrace();
            return CheckCertificationResponseDto.databaseError();
        }

        return CheckCertificationResponseDto.success();
    }

    // 비밀번호 변경
    public ResponseEntity<? super UpdatePasswordResponseDto> updatePassword(UpdatePasswordRequestDto requestDto, Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
            // 제공된 비밀번호가 기존 비밀번호와 같지 않으면 오류 메시지 반환
            if (!bCryptPasswordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) return UpdatePasswordResponseDto.wrongPassword();

            user.setPassword(bCryptPasswordEncoder.encode(requestDto.getNewPassword()));
            userRepository.save(user);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }

        return UpdatePasswordResponseDto.success();
    }

    // 유저네임 변경
    public ResponseEntity<? super UpdateUsernameResponseDto> updateUsername(UpdateUsernameRequestDto requestDto, Long userId) {
        try {
            // 이미 있는 유저네임인 경우 오류 메시지 반환
            if (userRepository.existsByUsername(requestDto.getUsername())) return UpdateUsernameResponseDto.duplicateId();

            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
            user.setUsername(requestDto.getUsername());
            userRepository.save(user);
        } catch (Exception e) {
            return UpdateUsernameResponseDto.databaseError();
        }

        return UpdateUsernameResponseDto.success();
    }

    // 유저네임 중복 검사
    public ResponseEntity<? super CheckUsernameResponseDto> checkUsername(String username) {
        try {
            boolean existsByEmail = userRepository.existsByUsername(username);
            if (!existsByEmail) {
                return CheckUsernameResponseDto.success();
            } else {
                return CheckUsernameResponseDto.duplicateId();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CheckUsernameResponseDto.databaseError();
        }
    }

    // 닉네임 변경
    public ResponseEntity<? super UpdateNicknameResponseDto> updateNickname(String nickname, Long userId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
            user.setNickname(nickname);
            userRepository.save(user);
        } catch (Exception e) {
            return UpdateNicknameResponseDto.databaseError();
        }
        return UpdateNicknameResponseDto.success();
    }
}
