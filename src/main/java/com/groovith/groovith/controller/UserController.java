package com.groovith.groovith.controller;

import com.groovith.groovith.dto.*;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    // 회원가입 요청
    @PostMapping("/join")
    public ResponseEntity<JoinResponseDto> join(@RequestBody JoinRequestDto joinRequestDto) {
        return userService.join(joinRequestDto);
    }

    /**
     * 현재 User 의 정보를 반환하는 API.
     * @return 성공 시 200(Ok) + CurrentUserDetailsDto | 유저가 존재하지 않는 경우 404(Not Found) + message
     */
    @GetMapping("/users/me")
    public ResponseEntity<CurrentUserDetailsDto> getCurrentUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(new CurrentUserDetailsDto(userDetails.getUser()), HttpStatus.OK);
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<UserDetailsResponseDto> getUserByUsername(@PathVariable String username, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(userService.getUserByUsername(username, userDetails.getUserId()), HttpStatus.OK);
    }

    // 이메일 중복 체크, 중복이 없을 시 200 SU, 중복이 존재하면 400 DI, 데이터베이스 오류 500 DBE
    @PostMapping("/auth/check-email")
    public ResponseEntity<CheckEmailResponseDto> checkEmail(@RequestBody CheckEmailRequestDto checkEmailRequestDto) {
        return userService.checkEmail(checkEmailRequestDto.getEmail());
    }

    // 이메일 인증 번호 요청
    @PostMapping("/auth/email-certification")
    public ResponseEntity<EmailCertificationResponseDto> emailCertification(@RequestBody @Valid EmailCertificationRequestDto requestDto) {
        return userService.emailCertification(requestDto);
    }

    /**
     * 현재 User 상태 변경 : PUBLIC -> PRIVATE or PRIVATE -> PUBLIC
     * */
    @PutMapping("/users/me/update/status")
    public ResponseEntity<?> updateStatus(@AuthenticationPrincipal CustomUserDetails userDetails){
        userService.updateStatus(userDetails.getUserId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 비밀번호 변경
    @PatchMapping("/users/me/update/password")
    public ResponseEntity<? super UpdatePasswordResponseDto> updatePassword(@RequestBody @Valid UpdatePasswordRequestDto requestDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.updatePassword(requestDto, userDetails.getUserId());
    }

    // 유저네임 변경
    @PatchMapping("/users/me/update/username")
    public ResponseEntity<? super UpdateUsernameResponseDto> updateUsername(@RequestBody @Valid UpdateUsernameRequestDto requestDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.updateUsername(requestDto, userDetails.getUserId());
    }

    // 유저네임 중복 확인
    @GetMapping("/users/check-username")
    public ResponseEntity<? super CheckUsernameResponseDto> usernameCheck(@RequestBody @Valid CheckUsernameRequestDto requestDto) {
        return userService.checkUsername(requestDto.getUsername());
    }

    // 닉네임 변경
    @PatchMapping("/users/me/update/nickname")
    public ResponseEntity<? super UpdateNicknameResponseDto> updateNickname(@RequestBody @Valid UpdateNicknameRequestDto requestDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.updateNickname(requestDto.getNickname(), userDetails.getUserId());
    }
}
