package com.groovith.groovith.controller;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomMemberStatus;
import com.groovith.groovith.domain.ChatRoomStatus;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.ChatRoomFullException;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.service.ChatRoomService;
import com.groovith.groovith.service.ImageService;
import com.groovith.groovith.service.MessageService;
import com.groovith.groovith.service.NotificationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final NotificationService notificationService;
    private final ImageService imageService;
    private final SimpMessageSendingOperations template;
    private final MessageService messageService;

    /**
     *  채팅방 생성
     * */
    @PostMapping("/chatrooms")
    public ResponseEntity<CreateChatRoomResponseDto> createChatRoom( @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CreateChatRoomRequestDto request) {
        ChatRoom chatRoom = chatRoomService.create(userDetails.getUserId(), request);
        CreateChatRoomResponseDto response = new CreateChatRoomResponseDto();
        response.setChatRoomId(chatRoom.getId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     *  채팅방 목록 조회
     * */
//    @GetMapping("/api/chatroom")
//    public ResponseEntity<Result> chatRooms(){
//        return new ResponseEntity<>(new Result(chatRoomService.findAllDesc()), HttpStatus.OK);
//    }

    /**
     * 내 채팅방 목록 조회
     */
    @GetMapping("/chatrooms/me")
    public ResponseEntity<ChatRoomDetailsListDto> getChatRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(chatRoomService.getChatRoomsById(userDetails.getUserId()), HttpStatus.OK);
    }


    /**
     * 채팅방 상세 조회
     * */
    @GetMapping("/chatrooms/{chatRoomId}")
    public ResponseEntity<ChatRoomDetailsDto> findChatRoomDetail(@PathVariable(name = "chatRoomId")Long chatRoomId){
        return new ResponseEntity<>(chatRoomService.findChatRoomDetail(chatRoomId), HttpStatus.OK);
    }

    /**
     * 채팅방 입장
     * */
    @PutMapping("/chatrooms/{chatRoomId}/enter")
    public ResponseEntity<?> enterChatRoom(@PathVariable(name = "chatRoomId") Long chatRoomId, @AuthenticationPrincipal CustomUserDetails userDetails){
        try {
            chatRoomService.enterChatRoom(userDetails.getUserId(), chatRoomId);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (ChatRoomFullException e){
            return new ResponseEntity<>("현재 채팅방이 최대인원으로 꽉 찼습니다",HttpStatus.FORBIDDEN);
        }
    }

    /**
     * 채팅방 퇴장
     * */
    @PutMapping("/chatrooms/{chatRoomId}/leave")
    public ResponseEntity<?> leaveChatRoom(@PathVariable(name = "chatRoomId") Long chatRoomId, @AuthenticationPrincipal CustomUserDetails userDetails){
        ChatRoomMemberStatus memberStatus = chatRoomService.leaveChatRoom(userDetails.getUserId(), chatRoomId);
        // 채팅방에 사람이 없을 시 채팅방 삭제, 채팅방 이미지 삭제
        if(memberStatus == ChatRoomMemberStatus.EMPTY){
            deleteChatRoom(chatRoomId);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/chatrooms/{chatRoomId}")
    public ResponseEntity<?> delete(@PathVariable(name = "chatRoomId")Long chatRoomId){
        deleteChatRoom(chatRoomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
    * 채팅방 참가자 목록 조회
    * */
    @GetMapping("/chatrooms/{chatRoomId}/members")
    public ResponseEntity<Result> findAllUser(@PathVariable(name = "chatRoomId")Long chatRoomId){
        return new ResponseEntity<>(new Result(chatRoomService.findAllUser(chatRoomId)), HttpStatus.OK);
    }


    /**
     * 채팅방으로 초대
     * */
    @PostMapping("/chatrooms/{chatRoomId}/members/{userId}")
    public ResponseEntity<?> inviteChatRoom(
            @PathVariable(name="chatRoomId") Long chatRoomId,
            @PathVariable(name = "userId")Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails){
        chatRoomService.invite(userDetails.getUserId(), userId, chatRoomId);

        // 알림 메세지 생성 및 저장 - (초대받은사람Id, 초대한사람Id, 채팅방Id)
        String notification = notificationService.createInviteNotification(userId, userDetails.getUserId(), chatRoomId);

        // 초대 알림 전송
        template.convertAndSend("/sub/api/notification/" + userId, notification);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 채팅방에 친구 초대
     * */
    @PostMapping("/chatrooms/{chatRoomId}/friends")
    public ResponseEntity<?> inviteFriends(
            @PathVariable Long chatRoomId,
            @RequestBody InviteFriendsRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    )
    {
        chatRoomService.inviteFriends(userDetails.getUserId(), chatRoomId, requestDto.getFriends());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 채팅방 권한 변경
     * */
    @PutMapping("/chatrooms/{chatRoomId}/permissions")
    public ResponseEntity<?> changePermission(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        chatRoomService.updatePermission(chatRoomId, userDetails.getUserId());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }


    /**
     * 채팅방 삭제 시
     * 1. 채팅방 이미지 삭제
     * 2. 메시지 삭제
     */
    public void deleteChatRoom(Long chatRoomId){
        // 기존 채팅방 이미지 삭제
        imageService.deleteChatRoomImageById(chatRoomId);
        // 채팅방 내 메시지 삭제
        messageService.deleteAllMessageInChatRoom(chatRoomId);
        // 채팅방 삭제
        chatRoomService.deleteChatRoom(chatRoomId);
    }
}
