package com.groovith.groovith.controller;

import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.InviteRequestDto;
import com.groovith.groovith.dto.InviteResponseDto;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.service.NotificationService;
import com.groovith.groovith.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class NotificationController {

    private final SimpMessageSendingOperations template;
    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * 채팅방에 user 초대시 초대받는 유저에게 알림
     * userid -> 채팅방에 초대받는 유저 id
     */
    @MessageMapping("/api/notification/{userId}")
    public ResponseEntity<?>  inviteNotification(@Payload InviteRequestDto inviteRequestDto, @DestinationVariable Long userId){
        InviteResponseDto inviteResponseDto = new InviteResponseDto();
        // 알림 메세지 생성 및 저장
        String notification = notificationService.createInviteNotification(inviteRequestDto.getInviteeId(), inviteRequestDto.getInviterId(), inviteRequestDto.getChatRoomId());

        inviteResponseDto.setMessage(notification);
        template.convertAndSend("/sub/api/notification/" + userId, inviteResponseDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
