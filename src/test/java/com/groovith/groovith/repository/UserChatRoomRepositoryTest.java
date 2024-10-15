package com.groovith.groovith.repository;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.groovith.groovith.domain.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserChatRoomRepositoryTest {

    @Autowired UserChatRoomRepository userChatRoomRepository;
    @Autowired UserRepository userRepository;
    @Autowired ChatRoomRepository chatRoomRepository;

    @Test
    public void findByUserIdAndChatRoomId(){
        //given
        User data = new User();
        data.setUsername("user");
        data.setPassword("1234");
        data.setRole("ROLE_USER");
        data.setStreaming(StreamingType.NONE);
        User user = userRepository.save(data);

        ChatRoom data1 = ChatRoom.builder()
                .name("room")
                .build();

        ChatRoom chatRoom = chatRoomRepository.save(data1);

        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);
        //when
        UserChatRoom findUserChatRoom = userChatRoomRepository
                .findByUserIdAndChatRoomId(user.getId(), chatRoom.getId())
                .orElseThrow(()->new IllegalArgumentException("UserChatRoom 이 없음"));

        //then
        Assertions.assertThat(userChatRoom).isEqualTo(findUserChatRoom);
    }

    @Test
    @DisplayName("UserChatRoomRepository.delete +  UserChatRoom.deleteUserChatRoom() 로 연관관계가 없어지는지 테스트")
    public void delete(){
        //given
        User data = new User();
        data.setUsername("user");
        data.setPassword("1234");
        data.setRole("ROLE_USER");
        data.setStreaming(StreamingType.NONE);
        User user = userRepository.save(data);

        ChatRoom data1 = ChatRoom.builder()
                .name("room")
                .build();
        ChatRoom chatRoom = chatRoomRepository.save(data1);

        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);

        //when
        UserChatRoom.deleteUserChatRoom(userChatRoom, user, chatRoom);

        //then
        Assertions.assertThat(userChatRoomRepository.findByUserIdAndChatRoomId(user.getId(), chatRoom.getId()))
                .isEqualTo(Optional.empty());
        Assertions.assertThat(user.getUserChatRoom().size()).isEqualTo(0);
        Assertions.assertThat(chatRoom.getUserChatRooms().size()).isEqualTo(0);

    }
}
