package com.groovith.groovith.repository;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChatRoomRepositoryTest {

    @Autowired ChatRoomRepository chatRoomRepository;

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

        //when

        //then

    }

    @Test
    public void findById(){
        //given

        //when

        //then

    }

    @Test
    public void deleteById(){
        //given

        //when

        //then

    }


    ChatRoom createChatRoom(String name){
        return ChatRoom.builder()
                .name(name)
                .chatRoomType(ChatRoomType.SONG)
                .build();
    }
}