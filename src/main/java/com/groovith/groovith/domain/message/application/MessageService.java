package com.groovith.groovith.domain.message.application;


import com.groovith.groovith.domain.chatRoom.dao.ChatRoomRepository;
import com.groovith.groovith.domain.chatRoom.dao.UserChatRoomRepository;
import com.groovith.groovith.domain.chatRoom.domain.UserChatRoom;
import com.groovith.groovith.domain.message.dao.MessageRepository;
import com.groovith.groovith.domain.message.dto.MessageDto;
import com.groovith.groovith.domain.message.dto.MessageListDto;
import com.groovith.groovith.domain.user.dao.UserRepository;
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

    public Long save(MessageDto messageDto){

        UserChatRoom userChatRoom = userChatRoomRepository
                .findByUserIdAndChatRoomId(messageDto.getUserId(), messageDto.getChatRoomId());
        return messageRepository.save(messageDto.toEntity(userChatRoom)).getId();
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
