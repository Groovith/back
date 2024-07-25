package com.groovith.groovith.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.groovith.groovith.controller.UserController;
import com.groovith.groovith.domain.*;
import com.groovith.groovith.dto.MessageDto;
import com.groovith.groovith.dto.MessageListDto;
import com.groovith.groovith.repository.MessageRepository;
import com.groovith.groovith.repository.UserChatRoomRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks MessageService messageService;
    @Mock private MessageRepository messageRepository;
    @Mock private UserChatRoomRepository userChatRoomRepository;

    @Test
    public void save(){
        //given
        UserChatRoom userChatRoom = createUserChatRoom();

        MessageDto messageDto = new MessageDto();
        messageDto.setContent("hi");
        messageDto.setType(MessageType.CHAT);
        messageDto.setUserId(1L);
        messageDto.setChatRoomId(1L);

        Message message = messageDto.toEntity(userChatRoom);

        //when
        when(userChatRoomRepository.findByUserIdAndChatRoomId(anyLong(), anyLong()))
                .thenReturn(Optional.of(userChatRoom));
        when(messageRepository.save(any(Message.class)))
                .thenReturn(message);

        Message savedMessage = messageService.save(messageDto);


        //then
        Assertions.assertThat(savedMessage).isEqualTo(message);
    }

    @Test
    public void findAllDesc(){
        //given
        UserChatRoom userChatRoom = createUserChatRoom();

        List<Message> data = new ArrayList<>();
        for(int i=0; i<5; i++){
            Message message = createMessage(userChatRoom);
            data.add(message);
        }
        List<MessageListDto>messageList = data.stream().map(MessageListDto::new).collect(Collectors.toList());
        //when
        when(messageRepository.findAllByChatRoomId(anyLong())).thenReturn(data);

        List<MessageListDto> findList = messageService.findAllDesc(1L);

        //then
        Assertions.assertThat(findList).isEqualTo(messageList);
        Assertions.assertThat(findList.size()).isEqualTo(5);

    }

    public UserChatRoom createUserChatRoom(){
        User user = new User();
        ChatRoom chatRoom = ChatRoom.builder()
                .name("room")
                .chatRoomType(ChatRoomType.SONG)
                .build();
        return UserChatRoom.setUserChatRoom(user, chatRoom);
    }

    public Message createMessage(UserChatRoom userChatRoom){
        return Message.builder()
                .content("1")
                .messageType(MessageType.CHAT)
                .userChatRoom(userChatRoom)
                .build();
    }
}