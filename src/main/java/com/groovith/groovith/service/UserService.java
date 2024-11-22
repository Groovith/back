package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.S3Directory;
import com.groovith.groovith.domain.enums.UserRelationship;
import com.groovith.groovith.domain.enums.UserStatus;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.provider.EmailProvider;
import com.groovith.groovith.repository.*;
import com.groovith.groovith.security.JwtUtil;
import com.groovith.groovith.service.Image.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FriendRepository friendRepository;
    private final CertificationRepository certificationRepository;
    private final PasswordResetCertificationRepository passwordResetCertificationRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailProvider emailProvider;


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
            Certification certification = certificationRepository.findById(email).orElse(null);
            if (certification == null || !certification.isCertificated()) return JoinResponseDto.certificationFail();

            // 새 유저 생성
            User user = new User();
            user.setUsername(username);
            user.setNickname(username);
            user.setPassword(bCryptPasswordEncoder.encode(password));
            user.setEmail(email);
            user.setRole("ROLE_USER");
            user.setImageUrl(S3Directory.USER.getDefaultImageUrl());
            user.setStatus(UserStatus.PUBLIC);

            // 유저 저장
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
            return JoinResponseDto.databaseError();
        }

        return JoinResponseDto.success();
    }

    // 회원탈퇴
    @Transactional
    public ResponseEntity<? super DeleteAccountResponseDto> deleteAccount(String password, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        // 비밀번호 확인
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) return UpdatePasswordResponseDto.wrongPassword();

        // 탈퇴 회원이 만든 채팅방이면 채팅방 삭제, 아니면 탈퇴 회원이 속해 있던 채팅방 인원 -1
        List<UserChatRoom> userChatRooms = userChatRoomRepository.findByUserId(user.getId());
        for (UserChatRoom userChatRoom : userChatRooms) {
            for (Message message : userChatRoom.getMessages()) {
                // 탈퇴회원 메세지 처리 - isUserDeleted 된 메세지를 조회할때 username = 알수없음 으로 표시
                message.setIsUserDeleted();
                // 메시지와 userchatroom 연관관계 제거(user 탈퇴시에 userchatroom이 같이 삭제될때 메시지는 그대로 두기위함)
                message.setUserChatRoomNull();
            }

            ChatRoom chatRoom = userChatRoom.getChatRoom();
            // 채팅방 만든사람이 탈퇴 회원 or 채팅방에 탈퇴회원만 있었을 경우 채팅방 삭제
            if (chatRoom.getMasterUserId().equals(user.getId()) || chatRoom.getCurrentMemberCount() <= 1) {
                chatRoomRepository.delete(chatRoom);
            } else {
                // 채팅방 인원 -1
                chatRoom.subUser();
            }
        }

        try {
            // 유저 프로필 이미지 있는 경우 삭제
            if (!user.getImageUrl().equals(S3Directory.USER.getDefaultImageUrl())) {
                s3Service.deleteFileFromS3Bucket(user.getImageUrl(), S3Directory.USER.getDirectory());
            }
            // 유저 삭제
            userRepository.delete(user);
        } catch (Exception e) {
            return DeleteAccountResponseDto.databaseError();
        }
        return DeleteAccountResponseDto.success();
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

    // 다른 유저 조회
    public UserDetailsResponseDto getUserByUsername(String username, Long userId) {
        // 현재 로그인중인 유저
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        // 조회하려는 유저
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        UserRelationship relationship = getUserRelationship(friendRepository.findFriendsIdsFromUser(user), user, findUser);
        UserDetailsResponseDto userDetailsResponseDto = new UserDetailsResponseDto(findUser, relationship);

        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowingId(userId, findUser.getId());
        // 팔로우 관계가 있을시에 값 업데이트, 기본은 NOFOLLOW
        follow.ifPresent(value -> userDetailsResponseDto.setStatus(value.getStatus()));
        return userDetailsResponseDto;
    }

    /**
     * 유저 status 변경
     */
    @Transactional
    public void updateStatus(Long userId) {
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
    private static String getCertificationNumber() {
        StringBuilder certificationNumber = new StringBuilder();
        for (int count = 0; count < 4; count++) certificationNumber.append((int) (Math.random() * 10));
        return certificationNumber.toString();
    }

    // 이메일 인증 번호 확인
    public ResponseEntity<CheckCertificationResponseDto> checkCertification(CheckCertificationRequestDto requestDto) {
        try {
            String email = requestDto.getEmail();
            String certificationNumber = requestDto.getCertificationNumber();

            // DB 에서 인증 객체 조회(findById = findByEmail)
            Certification certification = certificationRepository.findById(email).orElse(null);
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
            if (!bCryptPasswordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword()))
                return UpdatePasswordResponseDto.wrongPassword();

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
            if (userRepository.existsByUsername(requestDto.getUsername()))
                return UpdateUsernameResponseDto.duplicateId();

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

    // 비밀번호 재설정 이메일 요청
    public ResponseEntity<? super PasswordResetEmailResponseDto> requestPasswordResetCertification(PasswordResetEmailRequestDto requestDto) {
        try {
            String email = requestDto.getEmail();
            // 해당 이메일로 가입된 회원이 있는지 확인
            if (!userRepository.existsByEmail(email)) return ResponseDto.noSuchUser();
            // 인증 코드 생성
            String code = UUID.randomUUID().toString();
            // 이메일 전송
            boolean isSuccess = emailProvider.sendPasswordResetMail(email, code);
            if (!isSuccess) return PasswordResetEmailResponseDto.mailSendFail();

            // 이메일과 인증 코드 DB 저장
            PasswordResetCertification passwordResetCertification = new PasswordResetCertification(email, code);
            passwordResetCertificationRepository.save(passwordResetCertification);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        return PasswordResetEmailResponseDto.success();
    }

    // 이메일 비밀번호 재설정
    @Transactional
    public ResponseEntity<? super PasswordResetResponseDto> resetPassword(PasswordResetRequestDto requestDto) {
        try {
            PasswordResetCertification certification = passwordResetCertificationRepository.findById(requestDto.getEmail()).orElseThrow();
            // 코드가 일치 하지 않는 경우 오류 메시지 반환
            if (!certification.getCode().equals(requestDto.getCode()))
                return PasswordResetResponseDto.certificationFail();
            // 코드가 일치 하는 경우 비밀번호 변경 + 인증 객체 삭제
            passwordResetCertificationRepository.delete(certification);
            User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow();
            user.setPassword(bCryptPasswordEncoder.encode(requestDto.getPassword()));
            userRepository.save(user);
        } catch (Exception e) {
            return ResponseDto.databaseError();
        }
        return PasswordResetResponseDto.success();
    }

    @Transactional
    // 프로필 사진 변경
    public void updateImageUrl(Long userId, String imageUrl) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.updateImageUrl(imageUrl);
    }


    private UserRelationship getUserRelationship(List<Long> friendsIdsFromUser, User user, User findUser) {
        if (friendsIdsFromUser.contains(findUser.getId())) {
            return UserRelationship.FRIEND;
        }
        if (user.equals(findUser)) {
            return UserRelationship.SELF;
        }
        return UserRelationship.NOT_FRIEND;
    }
}
