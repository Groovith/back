package com.groovith.groovith.domain;

import com.groovith.groovith.domain.UserChatRoom;
import com.groovith.groovith.domain.FollowEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;

    @OneToMany(mappedBy = "follower")
    private Set<FollowEntity> following = new HashSet<>();

    @OneToMany(mappedBy = "following")
    private Set<FollowEntity> followers = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private List<UserChatRoom> userChatRoom = new ArrayList<>();
}
