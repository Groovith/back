package com.groovith.groovith.repository;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.enums.ChatRoomPrivacy;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChatRoomRepositoryTest {

    @Autowired private ChatRoomRepository chatRoomRepository;

    @BeforeEach
    void setUp(){
        chatRoomRepository.deleteAll();
    }

    @Test
    public void save(){
        //given
        String name = "room";
        ChatRoom chatRoom = createChatRoom(name);

        //when
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);

        //then
        Assertions.assertThat(savedChatRoom).isEqualTo(chatRoom);
        Assertions.assertThat(savedChatRoom.getName()).isEqualTo(name);
    }

    @Test
    public void findAllDesc(){
        //given
        ChatRoom chatRoom1 = createChatRoom("room1");
        ChatRoom chatRoom2 = createChatRoom("room2");
        ChatRoom chatRoom3 = createChatRoom("room3");
        ChatRoom chatRoom4 = createChatRoom("room4");

        chatRoomRepository.save(chatRoom1);
        chatRoomRepository.save(chatRoom2);
        chatRoomRepository.save(chatRoom3);
        chatRoomRepository.save(chatRoom4);
        //when
        List<ChatRoom> chatRoomList = chatRoomRepository.findAllDesc();

        //then
        Assertions.assertThat(chatRoomList.size()).isEqualTo(4);
        Assertions.assertThat(chatRoomList.get(3).getName()).isEqualTo("room1");
        Assertions.assertThat(chatRoomList.get(2).getName()).isEqualTo("room2");
        Assertions.assertThat(chatRoomList.get(1).getName()).isEqualTo("room3");
        Assertions.assertThat(chatRoomList.get(0).getName()).isEqualTo("room4");
    }

    @Test
    public void findById(){
        //given
        ChatRoom chatRoom = createChatRoom("room");
        chatRoomRepository.save(chatRoom);

        //when
        ChatRoom findChatRoom = chatRoomRepository.findById(chatRoom.getId())
                .orElseThrow(()-> new ChatRoomNotFoundException(chatRoom.getId()));

        //then
        Assertions.assertThat(findChatRoom).isEqualTo(chatRoom);
        Assertions.assertThat(findChatRoom.getId()).isEqualTo(chatRoom.getId());
    }

    @Test
    public void deleteById(){
        //given
        ChatRoom chatRoom = createChatRoom("room");
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        Long id = savedChatRoom.getId();

        //when
        chatRoomRepository.deleteById(id);

        //then
        Assertions.assertThat(chatRoomRepository.findById(id))
                .isEqualTo(Optional.empty());
    }

//    @Test
//    public void findChatRoomByNameContaining(){
//        //given
//        ChatRoom chatRoom1 = createChatRoom("test1");
//        ChatRoom chatRoom2 = createChatRoom("test2");
//        ChatRoom chatRoom3 = createChatRoom("wrong");
//
//        chatRoomRepository.save(chatRoom1);
//        chatRoomRepository.save(chatRoom2);
//        chatRoomRepository.save(chatRoom3);
//        //when
//        List<ChatRoom> chatRoomList = chatRoomRepository.findChatRoomByNameContaining("test");
//
//        //then
//        Assertions.assertThat(chatRoomList.size()).isEqualTo(2);
//        // 검색결과 1, 2번 포함해야함
//        Assertions.assertThat(chatRoomList).containsOnly(chatRoom1, chatRoom2);
//    }

    ChatRoom createChatRoom(String name){
        return ChatRoom.builder()
                .name(name)
                .privacy(ChatRoomPrivacy.PUBLIC)
                //.chatRoomType(ChatRoomType.SONG)
                .build();
    }
}