package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.MessageType;
import com.groovith.groovith.domain.enums.StreamingType;
import com.groovith.groovith.domain.enums.UserChatRoomStatus;
import com.groovith.groovith.domain.enums.UserStatus;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.MessageRepository;
import com.groovith.groovith.repository.UserChatRoomRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
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

    @InjectMocks
    MessageService messageService;
    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserChatRoomRepository userChatRoomRepository;

    @Value("${cloud.aws.s3.defaultUserImageUrl}")
    private String DEFAULT_IMG_URL;

    @Test
    public void save() {
        //given
        Long userId = 1L;
        Long chatRoomId = 1L;
        String content = "Hi";

        ChatRoom chatRoom = createChatRoom();
        User user = createUser(DEFAULT_IMG_URL);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);

        MessageDto messageDto = createMessageDto(user, chatRoom, content);

        Message message = Message.setMessage(content, MessageType.CHAT, userChatRoom, chatRoomId, DEFAULT_IMG_URL);

        //when
        when(userChatRoomRepository.findByUserIdAndChatRoomId(user.getId(), chatRoom.getId()))
                .thenReturn(Optional.of(userChatRoom));

        MessageResponseDto messageResponseDto = messageService.saveMessage(messageDto);


        //then
        Assertions.assertThat(messageResponseDto.getMessageId()).isEqualTo(message.getId());
    }

    @Test
    public void findMessages() {
        //given
        ChatRoom chatRoom = createChatRoom();
        User user = createUser(DEFAULT_IMG_URL);

        List<Message> data = new ArrayList<>();
        List<MessageResponseDto> savedMessages = new ArrayList<>();


        for (int i = 0; i < 5; i++) {
            Message message = createMessage(user, chatRoom, "message" + i);
            MessageResponseDto dto = MessageResponseDto.builder()
                    .messageId(message.getId())
                    .content(message.getContent())
                    .type(message.getMessageType())
                    .createdAt(message.getCreatedAt())
                    .imageUrl(message.getImageUrl())
                    .username(message.getUserChatRoom().getUser().getUsername())
                    .build();
            savedMessages.add(dto);
            data.add(createMessage(user, chatRoom, "message" + i));
        }

        MessageListResponseDto messageListResponseDto = new MessageListResponseDto(savedMessages);

        //when
        when(messageRepository.findMessages(chatRoom.getId(), null)).thenReturn(new SliceImpl<>(data));
        MessageListResponseDto findList = messageService.findMessages(chatRoom.getId(), null);

        //then
        Assertions.assertThat(findList).isEqualTo(messageListResponseDto);

    }


    @Test
    @DisplayName("프로필 사진 변경 시 변경 이전에 저장된 메시지의 imageUrl 과 새로 저장된 메시지의 imageUrl이 같아야한다")
    public void changeProfileImg() {
        //given
        User user = createUser(DEFAULT_IMG_URL);
        ChatRoom chatRoom = createChatRoom();
        String newImageUrl = "newImageUrl";
        List<MessageResponseDto> savedMessages = new ArrayList<>();
        List<Message> massages = new ArrayList<>();

        //when
        MessageDto messageDto = createMessageDto(user, chatRoom, "message");
        Message message = createMessage(user, chatRoom, "message");
        massages.add(message);

        // 프로필 사진 변경
        user.setImageUrl(newImageUrl);
        MessageDto newMessageDto = createMessageDto(user, chatRoom, "message");
        Message newMessage = createMessage(user, chatRoom, "message");
        massages.add(newMessage);

        when(messageRepository.findMessages(chatRoom.getId(), null)).thenReturn(new SliceImpl<>(massages));
        MessageListResponseDto messageListResponseDto = messageService.findMessages(chatRoom.getId(), null);

        //then
        Assertions.assertThat(messageListResponseDto.getMessages().get(0).getImageUrl())
                .isEqualTo(messageListResponseDto.getMessages().get(1).getImageUrl());
    }


    public ChatRoom createChatRoom() {
        return ChatRoom.builder()
                .name("room")
                .build();
    }

    public User createUser(String imageUrl) {
        User user = new User();
        user.setUsername("username");
        user.setNickname("nickname");
//        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setEmail("email");
        user.setRole("ROLE_USER");
        user.setStreaming(StreamingType.NONE);
        user.setImageUrl(imageUrl);
        user.setStatus(UserStatus.PUBLIC);
        return new User();
    }

    public Message createMessage(User user, ChatRoom chatRoom, String content) {
        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);

        return Message.setMessage(content, MessageType.CHAT, userChatRoom, chatRoom.getId(), user.getImageUrl());
    }

    public MessageDto createMessageDto(User user, ChatRoom chatRoom, String content) {
        return MessageDto.builder()
                .content(content)
                .username(user.getUsername())
                .userId(user.getId())
                .chatRoomId(chatRoom.getId())
                .imageUrl(user.getImageUrl())
                .type(MessageType.CHAT)
                .build();
    }
}