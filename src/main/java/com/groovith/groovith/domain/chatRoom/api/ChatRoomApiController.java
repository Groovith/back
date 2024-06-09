package com.groovith.groovith.domain.chatRoom.api;

import com.groovith.groovith.domain.chatRoom.application.ChatRoomService;
import com.groovith.groovith.domain.chatRoom.dto.ChatRoomDetailDto;
import com.groovith.groovith.domain.chatRoom.dto.CreateChatRoomRequestDto;
import com.groovith.groovith.domain.chatRoom.dto.CreateChatRoomResponseDto;
import com.groovith.groovith.domain.chatRoom.dto.EnterChatRoomRequestDto;
import com.groovith.groovith.domain.message.application.MessageService;
import com.groovith.groovith.domain.user.application.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatRoomApiController {

    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final MessageService messageService;

    /**
     *  채팅방 생성
     * */
    @PostMapping("/api/chat/chatroom")
    public CreateChatRoomResponseDto createChatRoom(@RequestBody CreateChatRoomRequestDto request) {
        Long chatRoomId = chatRoomService.create(request);

        return CreateChatRoomResponseDto.builder().chatRoomId(chatRoomId).build();
    }

    /**
     *  채팅방 목록 조회
     * */
    @GetMapping("/api/chat/chatroom")
    public Result chatRooms(){
        return new Result(chatRoomService.findAllDesc());
    }


    /**
     * 채팅방 상세 조회
     * */
    @GetMapping("/api/chat/chatroom/{chatRoomId}")
    public ChatRoomDetailDto findChatRoomDetail(@PathVariable(name = "chatRoomId")Long chatRoomId){
        return chatRoomService.findChatRoomDetail(chatRoomId);
    }

    /**
     * 채팅방 입장
     * */
    @PostMapping("api/chat/chatroom/{chatRoomId}")
    public void enterChatRoom(@RequestBody EnterChatRoomRequestDto request){
        chatRoomService.enterChatRoom(request.getUserId(), request.getChatRoomId());
    }


    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/api/chat/chatroom/{chatRoomId}")
    public void deleteChatRoom(@PathVariable(name = "chatRoomId")Long chatRoomId){
        chatRoomService.deleteChatRoom(chatRoomId);
    }


    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }
}
