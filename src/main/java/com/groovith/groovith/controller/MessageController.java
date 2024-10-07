package com.groovith.groovith.controller;



import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.MessageListResponseDto;
import com.groovith.groovith.dto.MessageRequestDto;
import com.groovith.groovith.dto.MessageResponseDto;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.service.MessageService;
import com.groovith.groovith.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

/**
 *  메세지 관련 컨르롤러
 * */

@Slf4j
@RequiredArgsConstructor
@RestController
public class MessageController {

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
     * 채팅방 채팅 조회(마지막 조회한 메세지 id 기준으로 size 만큼 불러오기)
     * */
    @GetMapping("/api/chat/{chatRoomId}")
    public ResponseEntity<MessageListResponseDto> messages(
            @PathVariable(name = "chatRoomId")Long chatRoomId,
            @RequestParam(required = false) Long lastMessageId) {
        return new ResponseEntity<>(messageService.findMessages(chatRoomId, lastMessageId), HttpStatus.OK);
    }
}
