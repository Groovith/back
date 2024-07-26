package com.groovith.groovith.repository;

import com.groovith.groovith.domain.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void save(){
        //given
        String username = "user";
        String password = "1234";

        User data = createUser(username, password);

        //when
        User user = userRepository.save(data);

        //then
        Assertions.assertThat(user.getUsername()).isEqualTo(username);
        Assertions.assertThat(user.getPassword()).isEqualTo(password);
        Assertions.assertThat(user.getRole()).isEqualTo("ROLE_USER");
        Assertions.assertThat(user.getStreaming()).isEqualTo(StreamingType.NONE);

    }

    @Test
    public void existsByUsername() throws Exception{
        //given
        String username = "user";
        String username1 = "ksy";
        String password = "1234";

        User data = createUser(username, password);
        userRepository.save(data);
        //when
        Boolean exist = userRepository.existsByUsername(username);
        Boolean notExist = userRepository.existsByUsername(username1);

        //then
        Assertions.assertThat(exist).isEqualTo(true);
        Assertions.assertThat(notExist).isEqualTo(false);
    }

    @Test
    public void findByUsername(){
        //given
        String username = "user";
        String password = "1234";
        User data = createUser(username, password);
        User user = userRepository.save(data);
        //when
        User findUser = userRepository.findByUsername(username)
                .orElseThrow(()->new IllegalArgumentException("유저가 없습니다. name:"+username));
        //then
        Assertions.assertThat(findUser).isEqualTo(user);
    }


    @Test
    public void findById(){
        //given
        User data = createUser("user", "1234");
        User user = userRepository.save(data);
        //when
        User findUser = userRepository.findById(user.getId())
                .orElseThrow(()->new IllegalArgumentException("유저가없습니다 user_id:"+user.getId()));
        //then
        Assertions.assertThat(findUser).isEqualTo(user);
    }


    public User createUser(String username, String password){
        User data = new User();
        data.setUsername(username);
        data.setPassword(password);
        data.setRole("ROLE_USER");
        data.setStreaming(StreamingType.NONE);
        return data;
    }

}