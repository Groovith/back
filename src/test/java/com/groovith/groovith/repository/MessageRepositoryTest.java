package com.groovith.groovith.repository;

import com.groovith.groovith.domain.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Autowired MessageRepository messageRepository;
    @Autowired ChatRoomRepository chatRoomRepository;
    @Autowired UserRepository userRepository;

    @Test
    public void save(){
        //given
        String content = "Hi";
        User user = new User();
        ChatRoom chatRoom = ChatRoom
                .builder()
                .name("room")
                .chatRoomType(ChatRoomType.SONG)
                .build();

        Message message = Message.builder()
                .content(content)
                .userChatRoom(UserChatRoom.setUserChatRoom(user, chatRoom))
                .messageType(MessageType.CHAT)
                .build();

        //when
        Message savedMessage = messageRepository.save(message);

        //then
        Assertions.assertThat(savedMessage).isEqualTo(message);
    }

    @Test
    public void findAllByChatRoomId(){
        //given
        User user = createUser();
        ChatRoom chatRoom = createChatRoom();
        ChatRoom chatRoom1 = createChatRoom();
        Message m1 = createMessage(user, chatRoom);
        Message m2 = createMessage(user, chatRoom);
        Message m3 = createMessage(user, chatRoom);
        Message m4 = createMessage(user, chatRoom1);
        messageRepository.save(m1);
        messageRepository.save(m2);
        messageRepository.save(m3);
        messageRepository.save(m4);
        List<Message> sendList = new ArrayList<>();
        sendList.add(m1);
        sendList.add(m2);
        sendList.add(m3);
        //when
        List<Message> messageList = messageRepository.findAllByChatRoomId(chatRoom.getId());

        //then
        Assertions.assertThat(messageList.size()).isEqualTo(3);
        Assertions.assertThat(messageList).isEqualTo(sendList);
    }

    public User createUser(){
        User user = new User();
        user.setUsername("user");
        user.setPassword("1234");
        user.setRole("ROLE_USER");
        user.setStreaming(StreamingType.NONE);
        userRepository.save(user);
        return user;
    }

    public ChatRoom createChatRoom(){
        ChatRoom chatRoom = ChatRoom
                .builder()
                .name("room")
                .chatRoomType(ChatRoomType.SONG)
                .build();
        chatRoomRepository.save(chatRoom);
        return chatRoom;
    }

    public Message createMessage(User user, ChatRoom chatRoom){
        Message message = Message.builder()
                .content("hi")
                .userChatRoom(UserChatRoom.setUserChatRoom(user, chatRoom))
                .messageType(MessageType.CHAT)
                .build();
        return message;
    }
}