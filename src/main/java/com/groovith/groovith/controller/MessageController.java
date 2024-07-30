package com.groovith.groovith.controller;


import com.groovith.groovith.service.ChatRoomService;
import com.groovith.groovith.service.MessageService;
import com.groovith.groovith.dto.MessageDto;
import com.groovith.groovith.service.UserService;
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
        /*log.info(String.format("roomid: %s, chatRoomId: %s, userId: %s", messageDto, messageDto.getChatRoomId(), messageDto.getUserId()));
        // 입장시
        if(messageDto.getType() == MessageType.JOIN){
           ChatRoomDetailDto detail = chatRoomService.findChatRoomDetail(messageDto.getChatRoomId());
           // 입장시 추가 로직 필요 + 예외처리 필요
        }
//        // 퇴장시
//        else if (messageDto.getType() == MessageType.LEAVE) {
//            chatRoomService.leaveChatRoom(messageDto.getUserId(), messageDto.getChatRoomId());
//        }
        //채팅시
        else if (messageDto.getType()== MessageType.CHAT) {
            log.info("Message sent to /sub/api/chat/" + messageDto.getChatRoomId());
            messageService.save(messageDto);
            template.convertAndSend("/sub/api/chat/" + messageDto.getChatRoomId(), messageDto);
        }*/

        // 메세지 저장 테스트
        //messageService.save(messageDto);

        template.convertAndSend("/sub/api/chat/" + messageDto.getChatRoomId(), messageDto);
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
