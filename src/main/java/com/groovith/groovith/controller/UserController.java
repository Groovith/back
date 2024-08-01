package com.groovith.groovith.controller;

import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.CurrentUserDetailsDto;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.service.UserService;
import com.groovith.groovith.dto.JoinDto;
import com.groovith.groovith.dto.UserDetailsResponseDto;
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

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody JoinDto joinDto) {
        try {
            userService.join(joinDto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
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
}
