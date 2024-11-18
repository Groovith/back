package com.groovith.groovith.repository;

import com.groovith.groovith.domain.*;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {
    // 실제 데이터베이스와 연결되있어서 수정 필요
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager em;

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

    }

    @Test
    public void existsByUsername(){
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

//    @Test
//    public void findByUsernameContaining(){
//        //given
//        User user1 = createUser("test1", "1234");
//        User user2 = createUser("test2", "1234");
//        User user3 = createUser("test3", "1234");
//
//        userRepository.save(user1);
//        userRepository.save(user2);
//        userRepository.save(user3);
//        em.flush();
//        em.clear();
//        //when
//        List<User> users = userRepository.findByUsernameContaining("test");
//
//        //then
//        Assertions.assertThat(users.stream().map(User::getUsername))
//                .containsExactly("test1", "test2", "test3");
//    }

    public User createUser(String username, String password){
        User data = new User();
        data.setUsername(username);
        data.setPassword(password);
        data.setRole("ROLE_USER");
        return data;
    }

}