package com.groovith.groovith.repository;

import com.groovith.groovith.domain.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
                .chatRoomType(ChatRoomType.SONG)
                .build();
        ChatRoom chatRoom = chatRoomRepository.save(data1);

        UserChatRoom userChatRoom = UserChatRoom.setUserChatRoom(user, chatRoom);
        //when
        UserChatRoom findUserChatRoom = userChatRoomRepository
                .findByUserIdAndChatRoomId(user.getId(), chatRoom.getId())
                .orElseThrow(()->new IllegalArgumentException("UserChatRoom 이 없음"));

        //then
        Assertions.assertThat(userChatRoom).isEqualTo(findUserChatRoom);
    }

}
