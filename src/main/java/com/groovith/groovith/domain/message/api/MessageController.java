package com.groovith.groovith.domain.message.api;


import com.groovith.groovith.domain.chatRoom.application.ChatRoomService;
import com.groovith.groovith.domain.chatRoom.dto.ChatRoomDetailDto;
import com.groovith.groovith.domain.message.application.MessageService;
import com.groovith.groovith.domain.message.domain.MessageType;
import com.groovith.groovith.domain.message.dto.MessageDto;
import com.groovith.groovith.domain.user.application.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 *  메세지 관련 컨르롤러
 * */

@Slf4j
@RequiredArgsConstructor
@RestController
public class MessageController {

    private final UserService userService;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private final SimpMessageSendingOperations template;

    /**
     * 메세지 전송
     * 메세지타입 : CHAT, JOIN, LEAVE, PLAYER
     * */
    @MessageMapping("/api/chat/{chatRoomId}")
    public void send(@Payload MessageDto messageDto)
    {
        if(messageDto.getType() == MessageType.JOIN){
            ChatRoomDetailDto detail = chatRoomService.findChatRoomDetail(messageDto.getChatRoomId());

        }
        if(messageDto.getType() == MessageType.CHAT) {
            messageService.save(messageDto);
            template.convertAndSend("/sub/api/chat/" + messageDto.getChatRoomId(), messageDto);
        }
    }

    /**
     * 채팅방의 모든 채팅 조회
     * */
    @GetMapping("/api/chat/{chatRoomId}")
    public Result messages(@PathVariable(name = "chatRoomId")Long chatRoomId ){
        return new Result(messageService.findAllDesc(chatRoomId));
    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        private T data;
    }

}
