package com.groovith.groovith.service;


import com.groovith.groovith.domain.Message;
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
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    public Message save(MessageDto messageDto){

        UserChatRoom userChatRoom = userChatRoomRepository
                .findByUserIdAndChatRoomId(messageDto.getUserId(), messageDto.getChatRoomId())
                .orElseThrow(()->new IllegalArgumentException(
                        "채팅방에 유저가 존재하지 않음 userId"+messageDto.getUserId()+" chatroomId:"+messageDto.getChatRoomId()));
        return messageRepository.save(messageDto.toEntity(userChatRoom));
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
