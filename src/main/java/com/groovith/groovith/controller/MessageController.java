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
 * 메세지 관련 컨르롤러
 */

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
     */
    @MessageMapping("/api/chat/{chatRoomId}")
    public void send(@Payload MessageRequestDto messageRequestDto, @DestinationVariable Long chatRoomId, SimpMessageHeaderAccessor headerAccessor) {
        // Stomp 헤더 토큰으로 송신 유저 찾기
        Long userId = (Long) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");
        if (userId == null) {
            throw new RuntimeException("User ID is missing in the session.");
        }
        Optional<User> user = userRepository.findById(userId);
        MessageDto messageDto = createMessageDto(chatRoomId, userId, user, messageRequestDto);
        MessageResponseDto messageResponseDto = messageService.saveMessage(messageDto);

        template.convertAndSend("/sub/api/chat/" + chatRoomId, messageResponseDto);
    }

    /**
     * 채팅방 메시지 무한 스크롤 조회(마지막 조회한 메세지 id 기준으로 20 만큼 불러오기)
     * 첫 채팅일 경우 lastMessageId = null, 자동으로 제일 최신 메시지로부터 20개 가져오기(message-id 내림차순)
     * 불러온 메시지 리스트의 메시지 중 제일 작은 id 값 메시지 -> lastMessageId
     * lastMessageId 기준으로 또 내림차순 20개 가져오기
     */
    @GetMapping("/api/chat/{chatRoomId}")
    public ResponseEntity<MessageListResponseDto> messages(
            @PathVariable(name = "chatRoomId") Long chatRoomId,
            @RequestParam(required = false) Long lastMessageId) {
        return new ResponseEntity<>(messageService.findMessages(chatRoomId, lastMessageId), HttpStatus.OK);
    }

    private MessageDto createMessageDto(Long chatRoomId, Long userId, Optional<User> user, MessageRequestDto messageRequestDto) {
        return MessageDto.builder()
                .content(messageRequestDto.getContent())
                .type(messageRequestDto.getType())
                .username(user.get().getUsername())
                .chatRoomId(chatRoomId)
                .userId(userId)
                .imageUrl(user.get().getImageUrl())
                .build();
    }
}
