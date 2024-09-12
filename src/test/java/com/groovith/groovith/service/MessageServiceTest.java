//package com.groovith.groovith.service;
//
//import com.fasterxml.jackson.annotation.JsonTypeInfo;
//import com.groovith.groovith.controller.UserController;
//import com.groovith.groovith.domain.*;
//import com.groovith.groovith.dto.MessageDto;
//import com.groovith.groovith.dto.MessageListDto;
//import com.groovith.groovith.repository.ChatRoomRepository;
//import com.groovith.groovith.repository.MessageRepository;
//import com.groovith.groovith.repository.UserChatRoomRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class MessageServiceTest {
//
//    @InjectMocks MessageService messageService;
//    @Mock private MessageRepository messageRepository;
//    @Mock private ChatRoomRepository chatRoomRepository;
//
//    @Test
//    public void save(){
//        //given
//        Long userId = 1L;
//        Long chatRoomId = 1L;
//        String content = "Hi";
//
//        ChatRoom chatRoom = ChatRoom.builder()
//                .name("room")
//                .build();
//        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
//
//        MessageDto messageDto = new MessageDto();
//        messageDto.setContent(content);
//        messageDto.setType(MessageType.CHAT);
//        messageDto.setUserId(userId);
//        messageDto.setChatRoomId(chatRoomId);
//
//        Message message = Message.setMessage(content, chatRoom, userId, MessageType.CHAT, messageDto.getUsername());
//
//        //when
//        when(chatRoomRepository.findById(anyLong()))
//                .thenReturn(Optional.of(chatRoom));
//        when(messageRepository.save(any(Message.class)))
//                .thenReturn(message);
//
//        MessageDto dto = messageService.createMessage(messageDto);
//
//
//        //then
//        Assertions.assertThat(savedMessage).isEqualTo(message);
//
//    }
//
//    @Test
//    public void findAllDesc(){
//        //given
//        ChatRoom chatRoom = createChatRoom();
//
//        List<Message> data = new ArrayList<>();
//        for(int i=0; i<5; i++){
//            Message message = createMessage(chatRoom);
//            data.add(message);
//        }
//        List<MessageListDto>messageList = data.stream().map(MessageListDto::new).collect(Collectors.toList());
//        //when
//        when(messageRepository.findAllByChatRoomId(anyLong())).thenReturn(data);
//
//        List<MessageListDto> findList = messageService.findAllDesc(1L);
//
//        //then
//        Assertions.assertThat(findList).isEqualTo(messageList);
//        Assertions.assertThat(findList.size()).isEqualTo(5);
//
//    }
//
//    public ChatRoom createChatRoom(){
//        return ChatRoom.builder()
//                .name("room")
//                .build();
//    }
//
//    public Message createMessage(ChatRoom chatRoom){
//        return Message.builder()
//                .content("content")
//                .messageType(MessageType.CHAT)
//                .chatRoom(chatRoom)
//                .build();
//    }
//}