package com.groovith.groovith.domain.user.domain;

import com.groovith.groovith.domain.follow.domain.FollowEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
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
}
