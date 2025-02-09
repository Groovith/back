package com.groovith.groovith.controller;

import com.groovith.groovith.service.NotificationService;
import com.groovith.groovith.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/notification")
@RestController
public class NotificationController {

    private final SimpMessageSendingOperations template;
    private final NotificationService notificationService;
    private final UserService userService;

    // ChatRoomController 에서 유저 초대시 알림도 같이 보내게끔 수정
//    /**
//     * 채팅방에 user 초대시 초대받는 유저에게 알림
//     * userid -> 채팅방에 초대받는 유저 id
//     */
//    @MessageMapping("/invite/{userId}")
//    public ResponseEntity<?>  inviteNotification(@Payload InviteRequestDto inviteRequestDto, @DestinationVariable Long userId){
//        InviteResponseDto inviteResponseDto = new InviteResponseDto();
//        // 알림 메세지 생성 및 저장
//        String notification = notificationService.createInviteNotification(inviteRequestDto.getInviteeId(), inviteRequestDto.getInviterId(), inviteRequestDto.getChatRoomId());
//
//        inviteResponseDto.setMessage(notification);
//        template.convertAndSend("/sub/api/notification/" + userId, inviteResponseDto);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    /**
//     * 유저에게 팔로우 신청 시 알림 발송
//     * */
//    @MessageMapping("/follow/{userId}")
//    public ResponseEntity<?> followNotification(@Payload )
//

}
