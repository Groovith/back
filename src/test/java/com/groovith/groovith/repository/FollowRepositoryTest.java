package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Follow;
import com.groovith.groovith.domain.enums.StreamingType;
import com.groovith.groovith.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FollowRepositoryTest {

    @Autowired FollowRepository followRepository;
    @Autowired UserRepository userRepository;

    @Test
    public void save(){
        //given
        User follower = createUser("follower", "1234");
        User following = createUser("following", "1234");

        Follow follow = createFollow(following, follower);
        //when
        Follow savedFollow = followRepository.save(follow);

        //then
        Assertions.assertThat(savedFollow).isEqualTo(follow);
        Assertions.assertThat(savedFollow.getFollower()).isEqualTo(follow.getFollower());
        Assertions.assertThat(savedFollow.getFollowing()).isEqualTo(follow.getFollowing());
    }

    @Test
    public void existsByFollowerAndFollowing() throws Exception{
        //given
        User notFollower=  createUser("notFollower", "1234");
        User follower = createUser("follower", "1234");
        User following = createUser("following", "1234");
        userRepository.save(follower);
        userRepository.save(following);
        userRepository.save(notFollower);

        Follow follow = createFollow(following, follower);
        followRepository.save(follow);
        //when
        Boolean exist = followRepository.existsByFollowerAndFollowing(follower, following);
        Boolean notExist = followRepository.existsByFollowerAndFollowing(notFollower, following);
        //then
        Assertions.assertThat(exist).isEqualTo(true);
        Assertions.assertThat(notExist).isEqualTo(false);
    }

    @Test
    public void deleteByFollowerAndFollowing() throws Exception{
        //given
        User follower = createUser("follower", "1234");
        User following = createUser("following", "1234");
        userRepository.save(follower);
        userRepository.save(following);

        Follow follow = createFollow(following, follower);
        followRepository.save(follow);

        //when
        followRepository.deleteByFollowerAndFollowing(follower, following);

        //then
        Assertions.assertThat(followRepository.existsByFollowerAndFollowing(follower, following)).isEqualTo(false);
    }

    public Follow createFollow(User following, User follower){
        Follow follow = new Follow();
        follow.setFollowing(following);
        follow.setFollower(follower);
        return follow;
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
