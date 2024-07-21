package com.groovith.groovith.controller;

import com.groovith.groovith.service.UserService;
import com.groovith.groovith.dto.JoinDto;
import com.groovith.groovith.dto.UserDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/user")
    public ResponseEntity<UserDetailsResponse> getCurrentUserDetails(@RequestHeader("access") String accessToken) {
        return userService.getCurrentUserDetails(accessToken);
    }
}
