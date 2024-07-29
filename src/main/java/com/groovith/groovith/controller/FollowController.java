package com.groovith.groovith.controller;

import com.groovith.groovith.service.FollowService;
import com.groovith.groovith.dto.FollowRequest;
import com.groovith.groovith.dto.FollowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    // 팔로잉 목록 조회
    @GetMapping("/users/{userId}/following")
    public ResponseEntity<FollowResponse> getFollowing(@PathVariable Long userId) {
        return new ResponseEntity<>(followService.getFollowing(userId), HttpStatus.OK);
    }

    // 팔로워 목록 조회
    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<FollowResponse> getFollowers(@PathVariable Long userId) {
        return new ResponseEntity<>(followService.getFollowers(userId), HttpStatus.OK);
    }

    // 팔로우
    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestBody FollowRequest request) {
        // 예외 처리 필요
        return followService.follow(request.getFollower(), request.getFollowing());
    }

    // 언팔로우
    @DeleteMapping("/follow")
    public ResponseEntity<?> unfollow(@RequestBody FollowRequest request) {
        return followService.unfollow(request.getFollower(), request.getFollowing());
    }
}
