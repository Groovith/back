package com.groovith.groovith.domain;

import com.groovith.groovith.dto.UserChatRoomDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="user")
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;
    private String email;

    @Column(name = "streaming", nullable = false)
    @Enumerated(EnumType.STRING)
    private StreamingType streaming;

    @Column(name = "spotify_refresh_token")
    private String spotifyRefreshToken;

    @OneToMany(mappedBy = "follower")
    private Set<Follow> following = new HashSet<>();

    @OneToMany(mappedBy = "following")
    private Set<Follow> followers = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private List<UserChatRoom> userChatRoom = new ArrayList<>();

    @Column(name = "image_url")
    String imageUrl;
    public UserChatRoomDto toUserChatRoomDto(User user){
        return new UserChatRoomDto(user);
    }
}
