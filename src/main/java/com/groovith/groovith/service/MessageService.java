package com.groovith.groovith.service;


import com.groovith.groovith.domain.Message;
import com.groovith.groovith.domain.UserChatRoom;
import com.groovith.groovith.dto.MessageListResponseDto;
import com.groovith.groovith.dto.MessageResponseDto;
import com.groovith.groovith.exception.UserChatRoomNotFoundException;
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

    private final MessageRepository messageRepository;
    private final UserChatRoomRepository userChatRoomRepository;

    // 메세지 생성 후 PRIVATE 이면 save, PUBLIC 이면 저장 x
    public MessageResponseDto createMessage(MessageDto messageDto){
        Long userId = messageDto.getUserId();
        Long chatRoomId = messageDto.getChatRoomId();

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId)
                .orElseThrow(()->new UserChatRoomNotFoundException(userId, chatRoomId));

        // 메세지 생성
        Message message = Message.setMessage(
                messageDto.getContent(), messageDto.getType(),userChatRoom, chatRoomId
        );


//        // chatRoomStatus == PRIVATE 일 경우에만 메세지 저장
//        if(chatRoom.getStatus() == ChatRoomStatus.PRIVATE){
//            messageRepository.save(message);
//        }

        // 채팅방 종류 상관없이 메시지 전체 저장
        messageRepository.save(message);

        // 메시지 반환 Dto
        MessageResponseDto messageResponseDto = new MessageResponseDto();
        messageResponseDto.setMessageId(message.getId());
        messageResponseDto.setChatRoomId(messageDto.getChatRoomId());
        messageResponseDto.setUserId(messageDto.getUserId());
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

        return new MessageListResponseDto(messages.stream()
                .map(message -> {
                    MessageDetailsResponseDto dto = new MessageDetailsResponseDto();
                    dto.setMessageId(message.getId());
                    dto.setContent(message.getContent());
                    dto.setType(message.getMessageType());
                    dto.setChatRoomId(message.getChatRoomId());
                    dto.setCreatedAt(message.getCreatedAt());
                    // 탈퇴한 유저 메세지라면
                    if(message.isUserDeleted()){
                        System.out.println("-------------------------"+message.getContent());
                        dto.setUserId(null);
                        dto.setUsername("알수없음");
                    } else{
                        dto.setUserId(message.getUserChatRoom().getUser().getId());
                        dto.setUsername(message.getUserChatRoom().getUser().getUsername());
                    }
                    return dto;
                }).toList());
    }


}
