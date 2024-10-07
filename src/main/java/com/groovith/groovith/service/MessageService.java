package com.groovith.groovith.service;


import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomStatus;
import com.groovith.groovith.domain.Message;
import com.groovith.groovith.dto.MessageListResponseDto;
import com.groovith.groovith.dto.MessageResponseDto;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.MessageRepository;
import com.groovith.groovith.dto.MessageDto;
import com.groovith.groovith.dto.MessageDetailsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 메세지 생성 후 PRIVATE 이면 save, PUBLIC 이면 저장 x
    public MessageResponseDto createMessage(MessageDto messageDto){

        Long chatRoomId = messageDto.getChatRoomId();
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                .orElseThrow(()->new ChatRoomNotFoundException(chatRoomId));

        // 메세지 생성
        Message message = Message.setMessage(
                messageDto.getContent(),chatRoom, messageDto.getUserId(), messageDto.getType(), messageDto.getUsername()
        );

        // chatRoomStatus == PRIVATE 일 경우에만 메세지 저장
        if(chatRoom.getStatus() == ChatRoomStatus.PRIVATE){
            messageRepository.save(message);
        }

        // 메시지 반환 Dto
        MessageResponseDto messageResponseDto = new MessageResponseDto();
        messageResponseDto.setMessageId(message.getId());
        messageResponseDto.setChatRoomId(message.getChatRoom().getId());
        messageResponseDto.setUserId(message.getUserId());
        messageResponseDto.setUsername(messageDto.getUsername());
        messageResponseDto.setContent(message.getContent());
        messageResponseDto.setType(message.getMessageType());
        messageResponseDto.setCreatedAt(message.getCreatedAt());
        messageResponseDto.setImageUrl(messageDto.getImageUrl());

        return messageResponseDto;
    }

    /**
     * 채팅방 채팅 조회
     * */
    @Transactional(readOnly = true)
    public MessageListResponseDto findMessages(Long chatRoomId, Long lastMessageId){
        Slice<Message> messages = messageRepository.findMessages(chatRoomId, lastMessageId);
        return new MessageListResponseDto(messages.stream().map(MessageDetailsResponseDto::new).toList());
    }


}
