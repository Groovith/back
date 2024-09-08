package com.groovith.groovith.controller;


import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomStatus;
import com.groovith.groovith.domain.Message;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.MessageRequestDto;
import com.groovith.groovith.dto.MessageResponseDto;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.service.ChatRoomService;
import com.groovith.groovith.service.MessageService;
import com.groovith.groovith.dto.MessageDto;
import com.groovith.groovith.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

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
    private final UserRepository userRepository;

    /**
     * 메세지 전송
     * 메세지타입 : CHAT, JOIN, LEAVE, PLAYER
     * */
    @MessageMapping("/api/chat/{chatRoomId}")
    public void send(@Payload MessageRequestDto messageRequestDto, @DestinationVariable Long chatRoomId, SimpMessageHeaderAccessor headerAccessor)
    {
        // Stomp 헤더 토큰으로 송신 유저 찾기
        Long userId = (Long) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");
        if (userId == null) {
            throw new RuntimeException("User ID is missing in the session.");
        }
        Optional<User> user = userRepository.findById(userId);


        //메시지 저장 Dto
        MessageDto messageDto = new MessageDto();
        System.out.println("chatRoomId: " + chatRoomId);
        messageDto.setChatRoomId(chatRoomId);
        messageDto.setUserId(userId);
        messageDto.setContent(messageRequestDto.getContent());
        messageDto.setType(messageRequestDto.getType());
        messageDto.setUsername(user.get().getUsername());
        messageDto.setImageUrl(user.get().getImageUrl());

        // PRIVATE 일 경우에만 채팅 저장
        MessageResponseDto  messageResponseDto = messageService.createMessage(messageDto);
//        // 메시지 반환 Dto
//        MessageResponseDto messageResponseDto = new MessageResponseDto();
//        messageResponseDto.setMessageId(message.getId());
//        messageResponseDto.setChatRoomId(message.getChatRoom().getId());
//        messageResponseDto.setUserId(message.getUserId());
//        messageResponseDto.setUsername(user.get().getUsername());
//        messageResponseDto.setContent(message.getContent());
//        messageResponseDto.setType(message.getMessageType());
//        messageResponseDto.setCreatedAt(message.getCreatedAt());
//        messageResponseDto.setImageUrl(user.get().getImageUrl());

        template.convertAndSend("/sub/api/chat/" + chatRoomId, messageResponseDto);
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
