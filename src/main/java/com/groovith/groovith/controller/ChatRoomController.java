package com.groovith.groovith.controller;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.service.ChatRoomService;
import com.groovith.groovith.dto.ChatRoomDetailDto;
import com.groovith.groovith.dto.CreateChatRoomRequestDto;
import com.groovith.groovith.dto.CreateChatRoomResponseDto;
import com.groovith.groovith.dto.EnterChatRoomRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     *  채팅방 생성
     * */
    @PostMapping("/api/chatroom")
    public ResponseEntity<CreateChatRoomResponseDto> createChatRoom(@RequestBody CreateChatRoomRequestDto request) {
        ChatRoom chatRoom = chatRoomService.create(request);
        CreateChatRoomResponseDto response = new CreateChatRoomResponseDto();
        response.setChatRoomId(chatRoom.getId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     *  채팅방 목록 조회
     * */
    @GetMapping("/api/chatroom")
    public ResponseEntity<Result> chatRooms(){
        return new ResponseEntity<>(new Result(chatRoomService.findAllDesc()), HttpStatus.OK);
    }


    /**
     * 채팅방 상세 조회
     * */
    @GetMapping("/api/chatroom/{chatRoomId}")
    public ResponseEntity<ChatRoomDetailDto> findChatRoomDetail(@PathVariable(name = "chatRoomId")Long chatRoomId){
        return new ResponseEntity<>(chatRoomService.findChatRoomDetail(chatRoomId), HttpStatus.OK);
    }

    /**
     * 채팅방 입장
     * */
    @PostMapping("api/chatroom/{chatRoomId}")
    public ResponseEntity<?> enterChatRoom(@RequestBody EnterChatRoomRequestDto request){
        chatRoomService.enterChatRoom(request.getUserId(), request.getChatRoomId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 채팅방 퇴장
     * */
    @PutMapping("api/chatroom/{chatRoomId}")
    public ResponseEntity<?> leaveChatRoom(@RequestBody EnterChatRoomRequestDto request){
        chatRoomService.leaveChatRoom(request.getUserId(), request.getChatRoomId());
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


    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }
}
