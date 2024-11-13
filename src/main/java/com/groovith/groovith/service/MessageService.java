package com.groovith.groovith.service;


import com.groovith.groovith.domain.Message;
import com.groovith.groovith.domain.UserChatRoom;
import com.groovith.groovith.dto.MessageListResponseDto;
import com.groovith.groovith.dto.MessageResponseDto;
import com.groovith.groovith.exception.UserChatRoomNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.MessageRepository;
import com.groovith.groovith.dto.MessageDto;
import com.groovith.groovith.dto.MessageDetailsResponseDto;
import com.groovith.groovith.repository.UserChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class MessageService {
    private final static Long DELETED_USER_ID = null;
    private final static String DELETED_USER_USERNAME = "알수없음";

    private final MessageRepository messageRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    public MessageResponseDto saveMessage(MessageDto messageDto){
        Long userId = messageDto.getUserId();
        Long chatRoomId = messageDto.getChatRoomId();

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(()->new UserChatRoomNotFoundException(userId, chatRoomId));

        // 메세지 생성
        Message message = Message.setMessage(
                messageDto.getContent(), messageDto.getType(),userChatRoom, chatRoomId, messageDto.getImageUrl()
        );

        // 채팅방 종류 상관없이 메시지 전체 저장
        messageRepository.save(message);

        return createMessageResponseDto(message);
    }

    /**
     * 채팅방 채팅 조회
     * */
    @Transactional(readOnly = true)
    public MessageListResponseDto findMessages(Long chatRoomId, Long lastMessageId){
        Slice<Message> messages = messageRepository.findMessages(chatRoomId, lastMessageId);

        return new MessageListResponseDto(messages.stream()
                .map(this::createMessageResponseDto).toList());
    }

    private MessageResponseDto createMessageResponseDto(Message message){
        return MessageResponseDto.builder()
                .messageId(message.getId())
                .chatRoomId(message.getChatRoomId())
                .userId(getUserIdByMessage(message))
                .username(getUsernameByMessage(message))
                .content(message.getContent())
                .type(message.getMessageType())
                .createdAt(message.getCreatedAt())
                .imageUrl(getUserImageUrlByMessage(message))
                .build();
    }

    private Long getUserIdByMessage(Message message){
        if(message.isUserDeleted()){
            return DELETED_USER_ID;
        }
        return message.getUserChatRoom().getUser().getId();
    }

    private String getUsernameByMessage(Message message){
        if(message.isUserDeleted()){
            return DELETED_USER_USERNAME;
        }
        return message.getUserChatRoom().getUser().getUsername();
    }

    private String getUserImageUrlByMessage(Message message){
        return message.getUserChatRoom().getUser().getImageUrl();
    }

    /**
     * 채팅방의 메시지 삭제
     * */
    public void deleteAllMessageInChatRoom(Long chatRoomId) {
        messageRepository.deleteByChatRoomId(chatRoomId);
    }
}
