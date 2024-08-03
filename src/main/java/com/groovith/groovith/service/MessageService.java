package com.groovith.groovith.service;


import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.Message;
import com.groovith.groovith.dto.MessageRequestDto;
import com.groovith.groovith.dto.MessageResponseDto;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.UserChatRoomRepository;
import com.groovith.groovith.domain.UserChatRoom;
import com.groovith.groovith.repository.MessageRepository;
import com.groovith.groovith.dto.MessageDto;
import com.groovith.groovith.dto.MessageListDto;
import com.groovith.groovith.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public Message save(MessageDto messageDto){

        Long chatRoomId = messageDto.getChatRoomId();
        ChatRoom chatRoom = chatRoomRepository.findById(messageDto.getChatRoomId())
                .orElseThrow(()->new ChatRoomNotFoundException(chatRoomId));

        Message message = Message.setMessage(
                messageDto.getContent(),chatRoom, messageDto.getUserId(), messageDto.getType(), messageDto.getUsername());

        return messageRepository.save(message);
    }

    /**
     * 채팅방 채팅 조회
     * */
    @Transactional(readOnly = true)
    public List<MessageListDto> findAllDesc(Long chatRoomId){

        return messageRepository.findAllByChatRoomId(chatRoomId).stream()
                .map(MessageListDto::new)
                .collect(Collectors.toList());
    }

}
