package com.groovith.groovith.controller;

import com.groovith.groovith.dto.CreateFollowNotificationResponseDto;
import com.groovith.groovith.service.FollowService;
import com.groovith.groovith.dto.FollowRequest;
import com.groovith.groovith.dto.FollowResponse;
import com.groovith.groovith.service.NotificationService;
import com.groovith.groovith.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;
    private final SimpMessageSendingOperations template;
    private final NotificationService notificationService;
    private final UserService userService;

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

        // 알림 메세지 생성 및 db 저장
        CreateFollowNotificationResponseDto responseDto =
                notificationService.createFollowNotification(request.getFollower(), request.getFollowing());

        // 예외처리 때문에 팔로우 먼저 한 후 알림 발송
        followService.follow(request.getFollower(), request.getFollowing());

        // 팔로우시 팔로우 요청 받은 사람에게 알림 발송
        template.convertAndSend("/sub/api/notification/" + responseDto.getFollowingId(), responseDto.getAlarm());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 언팔로우
    @DeleteMapping("/follow")
    public ResponseEntity<?> unfollow(@RequestBody FollowRequest request) {
        return followService.unfollow(request.getFollower(), request.getFollowing());
    }
}
