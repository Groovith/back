package com.groovith.groovith.controller;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.service.ChatRoomService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     *  채팅방 생성
     * */
    @PostMapping("/api/chatroom")
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
    @GetMapping("/api/chatroom")
    public ResponseEntity<ChatRoomDetailsListDto> getChatRooms(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return new ResponseEntity<>(chatRoomService.getChatRoomsById(userDetails.getUserId()), HttpStatus.OK);
    }


    /**
     * 채팅방 상세 조회
     * */
    @GetMapping("/api/chatroom/{chatRoomId}")
    public ResponseEntity<ChatRoomDetailsDto> findChatRoomDetail(@PathVariable(name = "chatRoomId")Long chatRoomId){
        return new ResponseEntity<>(chatRoomService.findChatRoomDetail(chatRoomId), HttpStatus.OK);
    }

    /**
     * 채팅방 입장
     * */
    @PostMapping("api/chatroom/{chatRoomId}")
    public ResponseEntity<?> enterChatRoom(@PathVariable(name = "chatRoomId") Long chatRoomId, @AuthenticationPrincipal CustomUserDetails userDetails){
        chatRoomService.enterChatRoom(userDetails.getUserId(), chatRoomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 채팅방 퇴장
     * */
    @PutMapping("api/chatroom/{chatRoomId}")
    public ResponseEntity<?> leaveChatRoom(@PathVariable(name = "chatRoomId") Long chatRoomId, @AuthenticationPrincipal CustomUserDetails userDetails){
        chatRoomService.leaveChatRoom(userDetails.getUserId(), chatRoomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/api/chatroom/{chatRoomId}")
    public ResponseEntity<?> deleteChatRoom(@PathVariable(name = "chatRoomId")Long chatRoomId){
        chatRoomService.deleteChatRoom(chatRoomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * 채팅방으로 초대
     * */
    @PostMapping("/api/chatrooms/{chatRoomId}/members/{userId}")
    public ResponseEntity<?> inviteChatRoom(
            @PathVariable(name="chatRoomId") Long chatRoomId, @PathVariable(name = "userId")Long userId, @AuthenticationPrincipal CustomUserDetails userDetails){
        chatRoomService.invite(userDetails.getUserId(), userId, chatRoomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }
}
