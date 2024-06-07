package com.groovith.groovith.domain.follow.api;

import com.groovith.groovith.domain.follow.application.FollowService;
import com.groovith.groovith.domain.follow.dto.FollowRequest;
import com.groovith.groovith.domain.follow.dto.FollowResponse;
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
    @GetMapping("/{username}/following")
    public ResponseEntity<FollowResponse> getFollowing(@PathVariable String username) {
        return new ResponseEntity<>(followService.getFollowing(username), HttpStatus.OK);
    }

    // 팔로워 목록 조회
    @GetMapping("/{username}/followers")
    public ResponseEntity<FollowResponse> getFollowers(@PathVariable String username) {
        return new ResponseEntity<>(followService.getFollowers(username), HttpStatus.OK);
    }

    // 팔로우
    @PostMapping("/follow")
    public ResponseEntity<?> follow(@RequestBody FollowRequest request) {
        // 예외 처리 필요
        return followService.follow(request.getFollower(), request.getFollowing());
    }

    // 언팔로우
    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollow(@RequestBody FollowRequest request) {
        return followService.unfollow(request.getFollower(), request.getFollowing());
    }
}
