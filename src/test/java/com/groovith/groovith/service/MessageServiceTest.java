package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.MessageType;
import com.groovith.groovith.domain.enums.StreamingType;
import com.groovith.groovith.domain.enums.UserChatRoomStatus;
import com.groovith.groovith.domain.enums.UserStatus;
import com.groovith.groovith.dto.MessageDetailsResponseDto;
import com.groovith.groovith.dto.MessageDto;
import com.groovith.groovith.dto.MessageListResponseDto;
import com.groovith.groovith.dto.MessageResponseDto;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.MessageRepository;
import com.groovith.groovith.repository.UserChatRoomRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks MessageService messageService;
    @Mock private MessageRepository messageRepository;
    @Mock private ChatRoomRepository chatRoomRepository;

    @Mock private UserChatRoomRepository userChatRoomRepository;

    @Value("${cloud.aws.s3.defaultUserImageUrl}")
    private String DEFAULT_IMG_URL;

    @Test
    public void save(){
        //given
        Long userId = 1L;
        Long chatRoomId = 1L;
        String content = "Hi";

        ChatRoom chatRoom = createChatRoom();
        User user = createUser();
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);

        MessageDto messageDto = new MessageDto();
        messageDto.setContent(content);
        messageDto.setType(MessageType.CHAT);
        messageDto.setUserId(user.getId());
        messageDto.setChatRoomId(chatRoom.getId());

        Message message = Message.setMessage(content, MessageType.CHAT, userChatRoom, chatRoomId, DEFAULT_IMG_URL);

        //when
        when(userChatRoomRepository.findByUserIdAndChatRoomId(user.getId(), chatRoom.getId()))
                .thenReturn(Optional.of(userChatRoom));

        MessageResponseDto messageResponseDto = messageService.createMessage(messageDto);


        //then
        Assertions.assertThat(messageResponseDto.getMessageId()).isEqualTo(message.getId());

    }

    @Test
    public void findMessages(){
        //given
        ChatRoom chatRoom = createChatRoom();
        User user = createUser();

        List<Message> data = new ArrayList<>();
        List<MessageDetailsResponseDto> messages = new ArrayList<>();
       

        for(int i=0; i<5; i++){
            Message message = createMessage("message"+i);
            MessageDetailsResponseDto dto = new MessageDetailsResponseDto();
            dto.setMessageId(message.getId());
            dto.setContent(message.getContent());
            dto.setType(message.getMessageType());
            dto.setChatRoomId(message.getChatRoomId());
            dto.setCreatedAt(message.getCreatedAt());
            dto.setImageUrl(message.getImageUrl());
            messages.add(dto);
            data.add(createMessage("message"+i));
        }

        MessageListResponseDto messageListResponseDto = new MessageListResponseDto(messages);

        //when
        when(messageRepository.findMessages(chatRoom.getId(), null)).thenReturn(new SliceImpl<>(data));
        MessageListResponseDto findList = messageService.findMessages(chatRoom.getId(), null);
        
        //then
        Assertions.assertThat(findList).isEqualTo(messageListResponseDto);

    }



    public ChatRoom createChatRoom(){
        return ChatRoom.builder()
                .name("room")
                .build();
    }

    public User createUser(){
        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
//        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setEmail("email");
        user.setRole("ROLE_USER");
        user.setStreaming(StreamingType.NONE);
        user.setImageUrl(DEFAULT_IMG_URL);
        user.setStatus(UserStatus.PUBLIC);
        return new User();
    }

    public Message createMessage(String content){
        User user = createUser();
        ChatRoom chatRoom = createChatRoom();

        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);

        return Message.setMessage(content, MessageType.CHAT, userChatRoom, chatRoom.getId(), DEFAULT_IMG_URL);
    }
}