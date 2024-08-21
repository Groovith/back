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
    public ResponseEntity<UserDetailsResponseDto> getUserByUsername(@PathVariable String username) {
        return new ResponseEntity<>(userService.getUserByUsername(username), HttpStatus.OK);
    }

    // 이메일 중복 체크, 중복이 없을 시 200 SU, 중복이 존재하면 400 DI, 데이터베이스 오류 500 DBE
    @PostMapping("/auth/email-check")
    public ResponseEntity<EmailCheckResponseDto> checkEmail(@RequestBody EmailCheckRequestDto emailCheckRequestDto) {
        return userService.checkEmail(emailCheckRequestDto.getEmail());
    }

    // 이메일 인증 번호 요청
    @PostMapping("/auth/email-certification")
    public ResponseEntity<EmailCertificationResponseDto> emailCertification(@RequestBody @Valid EmailCertificationRequestDto requestDto) {
        return userService.emailCertification(requestDto);
    }

    // 이메일 인증 확인 요청
    @PostMapping("/auth/check-certification")
    public ResponseEntity<CheckCertificationResponseDto> checkCertification(@RequestBody @Valid CheckCertificationRequestDto requestDto) {
        return userService.checkCertification(requestDto);
    }
}
